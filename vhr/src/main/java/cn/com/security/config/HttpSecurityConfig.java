package cn.com.security.config;

import cn.com.security.customizer.HttpSecurityCustomizer;
import cn.com.security.decision.CustomizeSecurityMetadataSource;
import cn.com.security.handler.LoginFailureHandler;
import cn.com.security.handler.LoginSuccessHandler;
import cn.com.security.handler.CustomizeAccessDeniedHandler;
import cn.com.security.constant.SecurityConstant;
import cn.com.security.provider.LoginDaoAuthenticationProviderConfig;
import cn.com.security.session.CustomizeSessionExpireStrategy;
import cn.com.security.session.SessionControlConfig;
import cn.com.security.verification.code.common.UnifiedCodeCheckFilter;
import cn.com.security.verification.code.sms.config.SmsCodeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.context.DelegatingApplicationListener;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author wyl
 * @create 2020-08-02 12:03
 */
@Configuration
public class HttpSecurityConfig implements HttpSecurityCustomizer {
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    @Autowired
    private LoginFailureHandler loginFailureHandler;
    @Autowired
    private CustomizeAccessDeniedHandler customizeAccessDeniedHandler;
    @Autowired
    private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;
    @Autowired
    private CustomizeSessionExpireStrategy expireStrategy;
    @Autowired
    private LoginDaoAuthenticationProviderConfig loginDaoAuthenticationProviderConfig;
    @Autowired
    private AccessDecisionManager accessDecisionManager;
    @Autowired
    private CustomizeSecurityMetadataSource metadataSource;
    @Autowired
    private SmsCodeConfig smsCodeConfig;
    @Autowired
    private UnifiedCodeCheckFilter unifiedCodeCheckFilter;
    @Autowired
    private SessionControlConfig sessionControlConfig;
    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    //    @Autowired
    //    private SecurityContextRepositoryCustomizer generatorSecurityContextRepository;
    //    @Bean
    //    // 生成控制 SecurityContext的类，是保存安全上下文的一种方式
    //    public SecurityContextRepositoryCustomizer generatorSecurityContextRepository (){
    //        return http -> {
    //            HttpSessionSecurityContextRepository httpSecurityRepository = new HttpSessionSecurityContextRepository();
    //            httpSecurityRepository
    //                    .setDisableUrlRewriting(false);
    //            httpSecurityRepository.setAllowSessionCreation(true);
    //            AuthenticationTrustResolver trustResolver = http
    //                    .getSharedObject(AuthenticationTrustResolver.class);
    //            if (trustResolver != null) {
    //                httpSecurityRepository.setTrustResolver(trustResolver);
    //            }
    //            http.setSharedObject(SecurityContextRepository.class,
    //                    httpSecurityRepository);
    //        };
    //    }

    public void accept(HttpSecurity http) throws Exception {
        // 循环登录
        this.solveCircularLogin(http);
        // 添加session的管理
        sessionControlConfig.accept(http);
        // 添加验证码逻辑
        http
                .addFilterBefore(unifiedCodeCheckFilter, UsernamePasswordAuthenticationFilter.class) // 统一验证码校验
                // form 表单登录
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl(SecurityConstant.IMAGE_CODE_LOGIN_PROCESSING_URL) // 这个配置基本上会被收集到usernamePasswordAuthenticationFilter中做url校验
                        .loginPage(SecurityConstant.LOGIN_ENTRY_POINT) // 这个配置可以配置为登录的逻辑url，但是目前没有需求
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                // 添加授权路径匹配规则和登录决策
                .authorizeRequests(authorizeRequests -> authorizeRequests
                        .antMatchers("/**")
                        .authenticated()
                        .accessDecisionManager(accessDecisionManager)
                        .withObjectPostProcessor(metadataSourceObjectPostProcessor())
                )

                // 异常控制
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customizeAccessDeniedHandler) // 没有权限时会走入这里
                        .authenticationEntryPoint(loginUrlAuthenticationEntryPoint) // 设置授权点,一般情况下应该默认为上面表单中设置的授权点，但是有些操作会导致授权点变化
                )

                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                )

//                .sessionManagement(sessionManagement -> sessionManagement
//                                .maximumSessions(5)
//                                .expiredSessionStrategy(expireStrategy)
//                        //                        .sessionRegistry()
//                )

                .csrf(csrf -> csrf.disable())


                // 添加密码登录daoAuthenticationProvider
                .apply(loginDaoAuthenticationProviderConfig)
                // 添加短信验证码认证流程
                .and().apply(smsCodeConfig)


        ;
    }

    /**
     * 解决循环登录
     */
    private void solveCircularLogin(HttpSecurity http) {
        AuthenticationManagerBuilder am = http.getSharedObject(AuthenticationManagerBuilder.class);
        am.parentAuthenticationManager(null);
    }

    /**
     * CustomizeSecurityMetadataSource
     * {@link CustomizeSecurityMetadataSource}
     */
    private ObjectPostProcessor<?> metadataSourceObjectPostProcessor() {
        return new ObjectPostProcessor<FilterSecurityInterceptor>() {
            @Override
            public <O extends FilterSecurityInterceptor> O postProcess(O filter) {
                filter.setSecurityMetadataSource(metadataSource);
                return filter;
            }
        };
    }


}
