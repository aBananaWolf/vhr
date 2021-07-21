package cn.com.security.session.cache;

import cn.com.bo.Hr;
import cn.com.service.CacheSessionRegistryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.*;

import static javax.swing.UIManager.get;

/**
 * 控制sessionInformation的注册表
 *
 * redis数据结构分为三个hash
 * 分别是
 * 1. hash key field(username) value principal)
 * 2. hash key field(username) value sessionIds)
 * 3. hash key field(sessionId) value sessionInformation)
 *
 * 不能使用json作为key，因为序列化的时候实体的属性顺序不是固定的
 *
 *
 * @author wyl
 * @create 2020-08-11 17:24
 */
@Slf4j
@Component
public class CacheSessionRegistry implements SessionRegistry, ApplicationListener<SessionDestroyedEvent>, CacheSessionRegistryService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public static final String VHR_RELATIONSHIP_USERNAME_BETWEEN_PRINCIPAL = "vhr:relationshipUsernameBetweenPrincipal:all";
    public static final String VHR_RELATIONSHIP_USERNAME_BETWEEN_SESSION_IDS = "vhr:relationshipUsernameBetweenSessionIds:all";
    public static final String VHR_RELATIONSHIP_SESSION_BETWEEN_SESSION_INFORMATION = "vhr:relationshipSessionBetweenSessionInformation:all";

    // 采用了三个Hash 结构，数据永世长存
    //    public static final long REGISTRY_CACHE_TIME = 3;
    //    public static final TimeUnit REGISTRY_CACHE_TIME_UNIT = TimeUnit.DAYS;
    //    public final long rCacheTime = REGISTRY_CACHE_TIME;
    //    public final TimeUnit rCacheUnit = REGISTRY_CACHE_TIME_UNIT;


    // Hash key(vhrPrincipal) filed(username) value(principal)
    private final String vhrUserPrincipal = VHR_RELATIONSHIP_USERNAME_BETWEEN_PRINCIPAL;
    // Hash key(vhrPrincipal) filed(username) value(sessionIds)
    private final String vhrUserSessionIds = VHR_RELATIONSHIP_USERNAME_BETWEEN_SESSION_IDS;
    // Hash key(vhrSession) fileds(sessionIds) value(sessionInformation)
    private final String vhrSessionInfo = VHR_RELATIONSHIP_SESSION_BETWEEN_SESSION_INFORMATION;


    /**
     * 获取所有的principal
     */
    @Override
    public List<Object> getAllPrincipals() {
        List<Object> keys = redisTemplate.boundHashOps(vhrUserPrincipal).values();
        if (keys != null)
            return keys;
        else return Collections.emptyList();
    }

    /**
     * 获取目标用户所有的SessionInformation
     * @param principal
     * @param includeExpiredSessions
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
        try {
            // 获取用户的sessionIds
            Object sessionIdsJson = redisTemplate.boundHashOps(vhrUserSessionIds).get(((Hr)principal).getUsername());
            if (sessionIdsJson == null) {
                return Collections.emptyList();
            }
            Collection sessionIds = objectMapper.readValue(sessionIdsJson.toString(), new TypeReference<HashSet<String>>() {});
            if (CollectionUtils.isEmpty(sessionIds)) {
                return Collections.emptyList();
            }
            // 根据sessionIds 获取所有的 SessionInformation
            return getSessionInformationList(includeExpiredSessions, sessionIds);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据principal获取用户所有的sessionInformation失败", e);
            }
        }
        return Collections.emptyList();
    }

    // 下面还有方法引用，所以抽出来作为单独的方法
    @SuppressWarnings("all")
    private List<SessionInformation> getSessionInformationList(boolean includeExpiredSessions, Collection sessionIds) throws JsonProcessingException {
        // 获取用户所有的SessionInformation
        List sessionInformationJsonList = redisTemplate.boundHashOps(vhrSessionInfo).multiGet(sessionIds);
        if (CollectionUtils.isEmpty(sessionInformationJsonList)) {
            return Collections.emptyList();
        }
        ArrayList<SessionInformation> sessionInformationList = new ArrayList<>(sessionInformationJsonList.size());
        // 判断过期
        for (Object sessionInformationJson : sessionInformationJsonList) {
            SessionInformation sessionInformation = objectMapper.readValue(sessionInformationJson.toString(), new TypeReference<CacheSessionInformation>() {});
            if (includeExpiredSessions || !sessionInformation.isExpired()) {
                sessionInformationList.add(sessionInformation);
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("成功根据principal获取用户所有的sessionInformation");
        }

        return sessionInformationList;
    }

    /**
     * 获取目标sessionId 的 SessionInformation
     * @param sessionId
     * @return
     */
    @Override
    public SessionInformation getSessionInformation(String sessionId) {
        Object SessionInformationJson = redisTemplate.boundHashOps(vhrSessionInfo).get(sessionId);
        if (SessionInformationJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(SessionInformationJson.toString(), new TypeReference<CacheSessionInformation>() {});
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据sessionId获取用户的sessionInformation失败", e);
            }
        }
        return null;
    }

    /**
     * 刷新一下时间，似乎没有太大作用
     * @param sessionId
     */
    @Override
    public void refreshLastRequest(String sessionId) {
        Object sessionInfoJson = redisTemplate.boundHashOps(vhrSessionInfo).get(sessionId);
        if (sessionInfoJson == null) {
            return;
        }
        try {
            SessionInformation sessionInformation = objectMapper.readValue(sessionInfoJson.toString(), new TypeReference<CacheSessionInformation>() {});
            if (sessionInformation != null) {
                sessionInformation.refreshLastRequest();
            }
            // 刷新
            BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(vhrSessionInfo);
            boundHashOps.put(sessionId, objectMapper.writeValueAsString(sessionInformation));
            if (log.isTraceEnabled()) {
                log.trace("成功将sessionInformation刷新回缓存");
            }
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据sessionId获取用户的sessionInformation失败", e);
            }
        }
    }

    /**
     * 多端登录下线功能，我们直接在这里做，而不是在ConcurrentSessionControlAuthenticationStrategy
     * @param sessionId
     * @param principal
     */
    @Override
    public void registerNewSession(String sessionId, Object principal) {
        if (this.getSessionInformation(sessionId) != null) {
            // 移除对应的sessionId
            this.removeSessionInformation(sessionId);
        }
        try {
            // 添加进sessionInfo
            redisTemplate.boundHashOps(vhrSessionInfo).put(sessionId, objectMapper.writeValueAsString(new CacheSessionInformation(principal, sessionId, new Date())));
            // 如果sessionIds为空则注册
            BoundHashOperations<String, Object, Object> vhrUserSessionIdOps = redisTemplate.boundHashOps(vhrUserSessionIds);
            String username = ((Hr) principal).getUsername();
            Object sessionIdsJson = vhrUserSessionIdOps.get(username);
            if (sessionIdsJson == null) {
                // 注册user - sessionIds
                vhrUserSessionIdOps.put(username, objectMapper.writeValueAsString(Collections.singleton(sessionId)));
                // 注册user - principal
                redisTemplate.boundHashOps(vhrUserPrincipal).put(username, objectMapper.writeValueAsString(principal));
                if (log.isDebugEnabled()) {
                    log.debug("创建了一个新的vhrPrincipal " + principal);
                }
            } else {
                // 不为空则更新
                List<String> sessionIds = objectMapper.readValue(sessionIdsJson.toString(), new TypeReference<List<String>>() {});
                sessionIds.add(sessionId);
                vhrUserSessionIdOps.put(username, objectMapper.writeValueAsString(sessionIds));

                if (log.isDebugEnabled()) {
                    log.debug("更新了一个新的sessionId 并成功写回缓存 " + username);
                }
                if (log.isInfoEnabled()) {
                    log.info("通过多端登录检测 " + username);
                }
            }
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据sessionId和principal注册新会话、多端登录检测失败", e);
            }
        }
    }

    /**
     * 删除目标会话关联信息
     * @param sessionId
     */
    @Override
    public void removeSessionInformation(String sessionId) {
        SessionInformation sessionInformation = getSessionInformation(sessionId);
        if (sessionInformation == null) {
            return;
        }
        try {
            // 删除掉 session 和 information 的 key value
            redisTemplate.boundHashOps(vhrSessionInfo).delete(sessionId);
            // 删除 Principal 对应的sessionId
            BoundHashOperations<String, Object, Object> vhrPrincipalOps = redisTemplate.boundHashOps(vhrUserSessionIds);
            String username = ((Hr) sessionInformation.getPrincipal()).getUsername();
            Object sessionIdsJson = vhrPrincipalOps.get(username);
            if (sessionIdsJson == null) {
                return;
            }
            HashSet<String> sessionIds = objectMapper.readValue(sessionIdsJson.toString(), new TypeReference<HashSet<String>>() {});
            if (sessionIds == null) {
                return;
            }
            sessionIds.remove(sessionId);

            if (!CollectionUtils.isEmpty(sessionIds)) {
                // 更新回去
                vhrPrincipalOps.put(username, objectMapper.writeValueAsString(sessionIds));
                return;
            }
            // 为空则清除
            vhrPrincipalOps.delete(username);
            redisTemplate.boundHashOps(vhrUserPrincipal).delete(username);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据sessionId删除用户的sessionInformation失败", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        String sessionId = event.getId();
        removeSessionInformation(sessionId);
    }

    /**
     * 用来刷新过期信息
     * @param sessionInformationList
     */
    public void multiUpdateSessionInformation(List<SessionInformation> sessionInformationList) {
        if (CollectionUtils.isEmpty(sessionInformationList)) {
            return;
        }

        HashMap<String, String> sessionIdsAndInfo = new HashMap<>(sessionInformationList.size());

        try {
            for (SessionInformation sessionInformation : sessionInformationList) {
                sessionIdsAndInfo.put(sessionInformation.getSessionId(), objectMapper.writeValueAsString(sessionInformation));
            }
            redisTemplate.boundHashOps(vhrSessionInfo).putAll(sessionIdsAndInfo);
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("根据List<SessionInformation>更新用户的sessionInformation失败", e);
            }
        }
    }

    // 踢出用户
    public void kickOutUser(Object principal) {
        if (principal != null)  {
            List<SessionInformation> userSessionInformationList = this.getAllSessions(principal, false);
            for (SessionInformation sessionInformation : userSessionInformationList) {
                sessionInformation.expireNow();
            }
            this.multiUpdateSessionInformation(userSessionInformationList);
        }
    }
}
