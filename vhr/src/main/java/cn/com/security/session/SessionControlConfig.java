package cn.com.security.session;

import cn.com.entities.HrEntity;
import cn.com.security.customizer.SessionControlCustomizer;
import cn.com.security.session.cache.CacheConcurrentSessionControlAuthenticationStrategy;
import cn.com.security.session.cache.CacheSessionRegistry;
import cn.com.service.impl.HrServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.context.DelegatingApplicationListener;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;

/**
 * 超级管理员和经理可以随意禁用hr用户，用户的下一次操作将会被登出
 * {@link HrServiceImpl#updateHr(HrEntity)}
 * @author wyl
 * @create 2020-08-10 14:56
 */
@Configuration
public class SessionControlConfig implements SessionControlCustomizer {
    @Autowired
    private CustomizeSessionExpireStrategy expireStrategy;
    @Autowired
    private CacheSessionRegistry sessionRegistry;
    @Autowired
    private CacheConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy;

    // 最大会话数量
    public static final int MAXIMUM_SESSION = 1;
    // true则为超过会话数量直接报出异常，false为超出会话数量会挤掉最近不活跃的会话
    public static final boolean EXCEPTION_IF_MAXIMUM_EXCEEDED = false;

    @Override
    public void accept(HttpSecurity http) throws Exception {
        this.getSessionRegistry(http);
    }

    private void getSessionRegistry(HttpSecurity http) throws Exception {
        registerDelegateApplicationListener(http, sessionRegistry);
        http
                .sessionManagement(sessionManagement -> sessionManagement
                        .withObjectPostProcessor(concurrentSessionControlAuthenticationStrategyPostProcessor())
                        .maximumSessions(MAXIMUM_SESSION)
                        .maxSessionsPreventsLogin(EXCEPTION_IF_MAXIMUM_EXCEEDED)
                        .expiredSessionStrategy(expireStrategy)
                        .sessionRegistry(sessionRegistry)
                );
    }

    private ObjectPostProcessor<?> concurrentSessionControlAuthenticationStrategyPostProcessor() {
        return new ObjectPostProcessor<ConcurrentSessionControlAuthenticationStrategy>() {
            @Override
            public <O extends ConcurrentSessionControlAuthenticationStrategy> O postProcess(O object) {
                concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(EXCEPTION_IF_MAXIMUM_EXCEEDED);
                concurrentSessionControlAuthenticationStrategy.setMaximumSessions(MAXIMUM_SESSION);
                return (O)concurrentSessionControlAuthenticationStrategy;
            }
        };
    }

    private void registerDelegateApplicationListener(HttpSecurity http,
                                                     ApplicationListener<?> delegate) {
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        if (context == null) {
            return;
        }
        if (context.getBeansOfType(DelegatingApplicationListener.class).isEmpty()) {
            return;
        }
        DelegatingApplicationListener delegating = context
                .getBean(DelegatingApplicationListener.class);
        SmartApplicationListener smartListener = new GenericApplicationListenerAdapter(
                delegate);
        delegating.addListener(smartListener);
    }
}
