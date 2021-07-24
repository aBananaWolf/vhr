package cn.com.security.verification.code.sms.common;

import cn.com.constant.exception.FailedEnum;
import cn.com.exception.UserIllegalOperationException;
import cn.com.security.verification.code.common.AbstractCodeOperationService;
import cn.com.security.verification.code.common.CodeDetails;
import cn.com.security.verification.code.common.CodeOperationService;
import cn.com.security.verification.code.common.UnifiedCodeCheckFilter;
import cn.com.security.verification.code.sms.raw.SmsAuthenticationFilter;
import cn.com.security.verification.code.sms.raw.SmsCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * 主要功能
 * 1.控制短信发送次数(默认根据手机号，也可以修改方法
 * {@link AbstractSmsCodeService#doGetMaximumSendCredential(HttpServletRequest)}
 * 变成根据ip，总之功能基本完成
 * )
 * 2.控制短信的验证次数
 *
 *
 * @author wyl
 * @create 2020-08-04 15:59
 */
@Slf4j
public abstract class AbstractSmsCodeService extends AbstractCodeOperationService {

    // 验证码过期时间
    public final static int EXPIRE_TIME = 300;
    public final static TimeUnit EXPIRE_TIME_UNIT = TimeUnit.SECONDS;

    /**
     *  锁时间，详细分为
     *  1.检查计数过期时间
     *  2.保存计数过期时间
     *  3.用户锁过期时间
     */
    public final static int LOCK_TIME=24;
    public final static TimeUnit LOCK_TIME_UNIT= TimeUnit.HOURS;

    // 发送次数和验证次数
    public final static int MAXIMUM_COUNT_OF_CHECKED_ON_THE_DAY = 11;
    public final static int MAXIMUM_COUNT_OF_SEND_ON_THE_DAY = 3;

    private final int checkCount = MAXIMUM_COUNT_OF_CHECKED_ON_THE_DAY;
    private final int saveCount = MAXIMUM_COUNT_OF_SEND_ON_THE_DAY;

    /**
     * 获取请求验证码
     * @param credential
     * @return
     */
    @Override
    public String obtainCode(HttpServletRequest credential) {
        return credential.getParameter(CodeOperationService.CODE_PARAM);
    }
    @Override
    public void saveCode(CodeDetails code, HttpServletRequest credential) throws AuthenticationException {
        this.saveCode((SmsCode) code,credential);
    }

    /**
     * 配合方法 {@link AbstractSmsCodeService#checkedUserLock(HttpServletRequest, CodeDetails, String)}
     * 完成控制发送次数和校验次数的功能
     * @param smsCode
     * @param credential
     * @throws AuthenticationException
     */
    public void saveCode(SmsCode smsCode, HttpServletRequest credential) throws AuthenticationException {
        long expireTime = smsCode.getInitialTime();
        TimeUnit timeUnit = smsCode.getTimeUnit();
        smsCode.eraseCredentials();
        // 没有锁住就可以保存
        try {
            this.applyPreProcess(credential, smsCode);
            // 每天11次重试机会，如果达到重试次数UnifiedCodeCheckFilter会调用本类的userIsLocked()锁定住，无法通过验证
            Integer maximumCountOfCheck= this.getTheMaximumCheckCredentialsForMayBeEmpty(credential);
            // MaximumCheckCount不要为null，检查过滤链为保存后检查，直接获取实体即可
            if (maximumCountOfCheck == null) {
                smsCode.setMaximumCheckCount(checkCount);
                this.setMaximumCountOfCheck(credential, String.valueOf(checkCount));
            } else {
                smsCode.setMaximumCheckCount(maximumCountOfCheck);
            }

            // 3次发送短信机会
            Integer maximumCountOfSend = this.getTheMaximumSendCredentialsForMayBeEmpty(credential);
            // MaximumSendCount为null即可，MaximumSendCount为保存前检查，保存前检查是无法获得实体中的属性的
            if (maximumCountOfSend == null) {
                // 剩下2次了, 剩下的次数会在checkedUserLock方法中扣减
                this.setMaximumCountOfSend(credential, String.valueOf(saveCount - 1));
            }
            this.doSaveCode(smsCode,this.getCredential(credential),expireTime,timeUnit,credential);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("保存短信验证码出错了");
            }
            throw new AuthenticationServiceException("保存短信验证码出错了");
        } catch (AuthenticationException e) {
            throw new AuthenticationServiceException(smsCode.userLockedTips());
        } catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("redis出现异常", ex);
            }
        }
        String phone = null;
        try {
             phone = this.getPhone(credential);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error("调用cn.com.security.verification.code.sms.common.AbstractSmsCodeService.getPhone方法异常");
            }
        }
        // 发送短信验证码，需不需要使用rabbitmq，这里取决于提供商的并发量提供，任何代码都有并发量大小(我们比较穷，用rabbitmq提高响应速度)
        try {
            this.doSendCode(smsCode.getCode(), phone);
        } catch (Exception e) {
            try {
                this.setMaximumCountOfSend(credential, String.valueOf(saveCount));
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("redis出现异常", e);
                }
            }
            throw new UserIllegalOperationException(FailedEnum.SMS_VERIFICATION_CODE_SENT);
        }
    }

    protected abstract void doSendCode(String code, String phone) throws Exception;

    abstract protected void doSaveCode(SmsCode smsCode, String credential, long expireTime, TimeUnit timeUnit, HttpServletRequest request) throws JsonProcessingException;

    /**
     * 子类不需要重写，这是获取缓存验证码的方法
     * @param credential
     * @return
     * @throws AuthenticationException
     */
    @Override
    public final SmsCode getCode(HttpServletRequest credential) throws AuthenticationException {
        String param = this.getCredential(credential);
        if (StringUtils.isEmpty(param)) {
            throw new AuthenticationServiceException("没有请求参数");
        }
        try {
            return this.doGetSmsCode(param,credential);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled())
                log.error("短信验证码json数据解析异常");
        }
        return null;
    }

    abstract protected SmsCode doGetSmsCode(String credential, HttpServletRequest request) throws JsonProcessingException, AuthenticationException;

    @Override
    public void removeCode(HttpServletRequest credential) {

    }

    // 获取主要参数的方法
    public String getCredential(HttpServletRequest request) throws AuthenticationException {
        String param = request.getParameter(SmsAuthenticationFilter.SPRING_SECURITY_FORM_PHONE_KEY);
        if (StringUtils.isEmpty(param)) {
            throw new AuthenticationServiceException("没有请求参数");
        }
        return param;
    }

    /**
     * 如果子类重写了这个方法，就走子类，不会有以上逻辑(那么子类就负责获取手机号并调用下面的重载方法)
     * @param request
     * @param cacheImageCode
     * @throws AuthenticationException
     * @throws JsonProcessingException
     */
    @Override
    public void applyPreProcess(HttpServletRequest request, CodeDetails cacheImageCode) throws AuthenticationException, JsonProcessingException {
        // 判断该类是否是AbstractSmsCodeService的超类、超接口或者就是该类
        // 不是AbstractSmsCodeService才做处理，基本上就是走这里了
        if (!this.getClass().isAssignableFrom(AbstractSmsCodeService.class)) {
            try {
                // 这些代码的目的是为了使用本抽象类的getCredential方法以获取手机号
                String phone = getPhone(request);
                checkedUserLock(request,cacheImageCode,phone);
            } catch (AuthenticationException e) {
                throw e;
            } catch (Throwable throwable) {
                if (log.isErrorEnabled()) {
                    log.error("调用cn.com.security.verification.code.sms.common.AbstractSmsCodeService.getPhone方法异常");
                }
            }
        }
    }

    private String getPhone(HttpServletRequest request) throws Throwable {
        MethodType methodType = MethodType.methodType(String.class,HttpServletRequest.class);
        Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        implLookup.setAccessible(true);
        MethodHandles.Lookup lookup = (MethodHandles.Lookup) (implLookup.get(null));
        MethodHandle mh = lookup.findSpecial(AbstractSmsCodeService.class, "getCredential", methodType, this.getClass());
        return ((String) mh.invoke(this, request)).toString().trim();
    }


    protected void checkedUserLock(HttpServletRequest request, CodeDetails cacheImageCode, String phone) throws AuthenticationException, JsonProcessingException {
        if (phone == null) {
            throw new AuthenticationServiceException("用户非法操作，请输入手机号");
        }

        if (getStringRedisTemplate().opsForValue().get(this.getSmsMaximumLock() + phone) != null) {
            throw new AuthenticationServiceException("账号已经被锁定住了！");
        }

        SmsCode smsCode = (SmsCode) cacheImageCode;
        // 非检查过滤链 消耗短信发送次数
        if (!UnifiedCodeCheckFilter.IS_CHECK_FILTER_CHAIN.equals(request.getAttribute(UnifiedCodeCheckFilter.IS_CHECK_FILTER_CHAIN))) {
            // AbstractSmsCodeService中调用save之前会调用一次本方法，如果是第一次请求是没有数据的
            Integer maximumCountOfSend = this.getTheMaximumSendCredentialsForMayBeEmpty(request);
            if (maximumCountOfSend != null) {
                // 后续的请求都会消耗次数
                if (maximumCountOfSend <= 0) {
                    this.userSendVerificationCodeLocked(phone);
                    throw new AuthenticationServiceException("账号已经被锁定住了！");
                } else {
                    // 发送次数-1
                    smsCode.setMaximumSaveCount((int)this.decrMaximumCountOfSend(request));
                }
            }
        } else {
            // 检查过滤链的逻辑，重试次数 - 1，达到 0 时，UnifiedCodeCheckFilter 会出现24小时内无重试机会提示，并且不能再发送短信。后续都是账号锁定提示
            if (smsCode.getMaximumCheckCount() == null) {
                throw new AuthenticationServiceException("非法请求");
            }
            // 每次检查都会消耗次数
            smsCode.setMaximumCheckCount((int) this.decrMaximumCountOfCheck(request));
            if (smsCode.getMaximumCheckCount() < 0) {
                this.userSendVerificationCodeLocked(phone);
            }
        }
    }

    protected void userSendVerificationCodeLocked(String phone){
        getStringRedisTemplate().opsForValue().set(this.getSmsMaximumLock() + phone,"true", LOCK_TIME, LOCK_TIME_UNIT);
    }

    abstract protected String getSmsMaximumLock();

    abstract protected StringRedisTemplate getStringRedisTemplate();

    // ------------------------------------------ 最大发送次数
    private Integer getMaximumCountOfSend(HttpServletRequest request) {
        String credential = this.getNonNullMaximumSendCredential(request);
        String cache = getStringRedisTemplate().opsForValue().get(credential);
        if (cache == null) {
            return null;
        }
        return Integer.parseInt(cache);
    }

    private long decrMaximumCountOfSend(HttpServletRequest request) {
        String credential = this.getNonNullMaximumSendCredential(request);
        return getStringRedisTemplate().opsForValue().decrement(credential);
    }

    private void setMaximumCountOfSend(HttpServletRequest request, String max) {
        String credential = this.getNonNullMaximumSendCredential(request);
        getStringRedisTemplate().opsForValue().set(credential,max,LOCK_TIME,LOCK_TIME_UNIT);
    }

    private String getNonNullMaximumSendCredential(HttpServletRequest request) {
        String maximumSend = this.doGetMaximumSendCredential(request);
        if (StringUtils.isEmpty(maximumSend)) {
            throw new AuthenticationServiceException("需要最大消息发送次数限制");
        }
        return maximumSend;
    }

    protected Integer getTheMaximumSendCredentialsForMayBeEmpty(HttpServletRequest request) {
        String max = this.doGetMaximumSendCredential(request);
        max = this.getStringRedisTemplate().opsForValue().get(max);
        if (max == null) {
            return null;
        }
        return Integer.parseInt(max);
    }

    abstract protected String doGetMaximumSendCredential(HttpServletRequest request);

    // ------------------------------------------ 最大检查次数
    private Integer getMaximumCountOfCheck(HttpServletRequest request) {
        String credential = this.getNonNullMaximumCheckCredential(request);
        String cache = getStringRedisTemplate().opsForValue().get(credential);
        if (cache == null) {
            return null;
        }
        return Integer.parseInt(cache);
    }
    private long decrMaximumCountOfCheck(HttpServletRequest request) {
        String credential = this.getNonNullMaximumCheckCredential(request);
        return getStringRedisTemplate().opsForValue().decrement(credential);
    }
    private void setMaximumCountOfCheck(HttpServletRequest request, String max) {
        String credential = this.getNonNullMaximumCheckCredential(request);
        getStringRedisTemplate().opsForValue().set(credential,max,LOCK_TIME,LOCK_TIME_UNIT);
    }
    private String getNonNullMaximumCheckCredential(HttpServletRequest request) {
        String maximumSend = this.doGetMaximumCheckCredential(request);
        if (StringUtils.isEmpty(maximumSend)) {
            throw new AuthenticationServiceException("需要最大消息检查次数限制");
        }
        return maximumSend;
    }

    protected Integer getTheMaximumCheckCredentialsForMayBeEmpty(HttpServletRequest request) {
        String max = this.doGetMaximumCheckCredential(request);
        max = this.getStringRedisTemplate().opsForValue().get(max);
        if (max == null) {
            return null;
        }
        return Integer.parseInt(max);
    }

    abstract protected String doGetMaximumCheckCredential(HttpServletRequest request);
}
