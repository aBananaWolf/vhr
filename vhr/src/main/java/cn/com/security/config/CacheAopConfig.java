package cn.com.security.config;

import cn.com.anno.CacheFlag;
import cn.com.controller.system.basic.PermitsController;
import cn.com.constant.cache.CacheTypeEnum;
import cn.com.security.decision.CustomizeSecurityMetadataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *  这是redisson读写锁的封装，是不需要的功能，项目中不需要这么高的安全性(如果需要这种安全性，考虑弃用redis)
 *  建议使用spring 抽象出来的注解
 *  以下的类会使用这个切面
 * {@link CustomizeSecurityMetadataSource}
 * {@link PermitsController}
 *
 * @author wyl
 * @create 2020-08-09 14:06
 */
@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy
public class CacheAopConfig {
    @Pointcut("@annotation(cn.com.anno.CacheFlag)")
    public void pointCut() {
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Around("pointCut()")
    public Object CacheFlagConfig(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 处理缓存key
        CacheFlag cacheFlag = method.getAnnotation(CacheFlag.class);
        String cachePrefix = cacheFlag.cachePrefix();
        String lockPrefix = cacheFlag.lockPrefix();
        Object arg = null;
        int i = cacheFlag.keyPosition();
        if (i != -1) {
            arg = joinPoint.getArgs()[i];
        }
        if (arg != null) {
            cachePrefix += arg;
            lockPrefix +=arg;
        }

        // 处理锁key
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockPrefix);
        RLock rLock;

        // 处理读写锁
        CacheTypeEnum cacheType = cacheFlag.cacheType();

        switch (cacheType) {
            case READ:
                // 查询缓存
                String json = redisTemplate.opsForValue().get(cachePrefix);
                // 获取返回值类型
                Type genericReturnType = method.getGenericReturnType();
                if (json != null) {
                    return this.parseJson(json, genericReturnType);
                }
                rLock = readWriteLock.readLock();
                // 加锁
                return getCache(joinPoint, cachePrefix, rLock, genericReturnType);
            case WRITE:
                rLock = readWriteLock.writeLock();
                // 添加写锁，避免读取脏数据
                rLock.lock();
                try {
                    // 失效模式(如果目标方法执行失败，只是缓存被失效)
                    redisTemplate.boundValueOps(cachePrefix).expire(-1, TimeUnit.SECONDS);
                    // 如果是修改先行，那么如果缓存执行失败了就会有脏数据产生，缓存中仍然是旧值
                    return joinPoint.proceed();
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("redisson写入缓存失败", e);
                    }
                } finally {
                    rLock.unlock();
                }
                break;
        }
        // 只要注解没标错不会走到这里
        return null;
    }

    private Object getCache(ProceedingJoinPoint joinPoint, String cachePrefix, RLock rLock, Type genericReturnType) throws Throwable {
        String json;
        rLock.lock();
        // 再次检测，可能已经被抢先缓存了
        json = redisTemplate.opsForValue().get(cachePrefix);
        Object proceed = null;
        try {
            if (json == null) {
                proceed = joinPoint.proceed();
                // 缓存空串,避免恶意攻击导致缓存穿透
                if (proceed == null) {
                    redisTemplate.opsForValue().set(cachePrefix, "", 15, TimeUnit.MINUTES);
                } else {
                    // 一周 + 六分钟
                    redisTemplate.opsForValue().set(cachePrefix, objectMapper.writeValueAsString(proceed), this.randomExpireTime(10086), TimeUnit.MINUTES);
                }
            } else {
                return this.parseJson(json, genericReturnType);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("redis读取数据失败", e);
            }
        } finally {
            rLock.unlock();
        }
        return proceed;
    }

    private Object parseJson(String json, Type type) {
        try {
            return objectMapper.readValue(json, new TypeReference<Object>() {
                @Override
                public Type getType() {
                    return type;
                }
            });
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("json解析错误", e);
            }
        }
        return null;
    }

    // 项目没有雪崩，但是还是加上了
    private int randomExpireTime(int expireTime) {
        Random random = new Random();
        return expireTime + random.nextInt(expireTime >> 2);
    }

}
