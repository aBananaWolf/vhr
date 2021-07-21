package cn.com.security.session.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 这个类重写了allowableSessionsExceeded方法，默认的父类方法中是不会将进行保存过期信息的，仅是更改实体的状态
 * {@link AbstractAuthenticationProcessingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
 *
 * @author wyl
 * @create 2020-08-12 11:40
 */
@Slf4j
@Component
public class CacheConcurrentSessionControlAuthenticationStrategy extends ConcurrentSessionControlAuthenticationStrategy {
    @Autowired
    private CacheSessionRegistry sessionRegistry;
    /**
     * @param sessionRegistry the session registry which should be updated when the
     * authenticated session is changed.
     */
    public CacheConcurrentSessionControlAuthenticationStrategy(CacheSessionRegistry sessionRegistry) {
        super(sessionRegistry);
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * 这个方法会在AbstractAuthenticationProcessingFilter中被调用
     * @param sessionInformationList
     * @param allowableSessions
     * @param registry
     * @throws SessionAuthenticationException
     */
    @Override
    protected void allowableSessionsExceeded(List<SessionInformation> sessionInformationList, int allowableSessions, SessionRegistry registry) throws SessionAuthenticationException {
        super.allowableSessionsExceeded(sessionInformationList, allowableSessions, registry);
        // 更新缓存的过期信息，非常重要

        sessionRegistry.multiUpdateSessionInformation(sessionInformationList);
        if (log.isInfoEnabled()) {
            // linkedHashMap
            List<Object> userList = sessionInformationList.stream().filter(info -> info.isExpired()).map(sessionInfo -> sessionInfo.getPrincipal()).collect(toList());
            log.info("过期掉的不活跃用户 "  + userList);
        }
    }
}
