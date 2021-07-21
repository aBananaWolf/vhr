package cn.com.security.verification.code.sms.config;

import cn.com.security.handler.LoginFailureHandler;
import cn.com.security.handler.LoginSuccessHandler;
import cn.com.security.service.SmsCodeUserDetailService;
import cn.com.security.verification.code.sms.raw.SmsAuthenticationFilter;
import cn.com.security.verification.code.sms.raw.SmsAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

/**
 * 配置
 * @author wyl
 * @create 2020-08-04 18:21
 */
@Component
public class SmsCodeConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private SmsCodeUserDetailService smsCodeUserDetailService;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 装配filter
        SmsAuthenticationFilter smsAuthenticationFilter = new SmsAuthenticationFilter();
        // 获取httpSecurity中共享的SessionAuthenticationStrategy
        smsAuthenticationFilter.setSessionAuthenticationStrategy(http.getSharedObject(SessionAuthenticationStrategy.class));
        smsAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        smsAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler);
        smsAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        // 装配provider
        SmsAuthenticationProvider smsAuthenticationProvider = new SmsAuthenticationProvider(smsCodeUserDetailService);
        // 配置
        http.addFilterBefore(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(smsAuthenticationProvider);
    }
}
