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
    //    // ???????????? SecurityContext????????????????????????????????????????????????
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
        // ????????????
        this.solveCircularLogin(http);
        // ??????session?????????
        sessionControlConfig.accept(http);
        // ?????????????????????
        http
                .addFilterBefore(unifiedCodeCheckFilter, UsernamePasswordAuthenticationFilter.class) // ?????????????????????
                // form ????????????
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl(SecurityConstant.IMAGE_CODE_LOGIN_PROCESSING_URL) // ????????????????????????????????????usernamePasswordAuthenticationFilter??????url??????
                        .loginPage(SecurityConstant.LOGIN_ENTRY_POINT) // ??????????????????????????????????????????url???????????????????????????
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                // ?????????????????????????????????????????????
                .authorizeRequests(authorizeRequests -> authorizeRequests
                        .antMatchers("/**")
                        .authenticated()
                        .accessDecisionManager(accessDecisionManager)
                        .withObjectPostProcessor(metadataSourceObjectPostProcessor())
                )

                // ????????????
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customizeAccessDeniedHandler) // ??????????????????????????????
                        .authenticationEntryPoint(loginUrlAuthenticationEntryPoint) // ???????????????,????????????????????????????????????????????????????????????????????????????????????????????????????????????
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


                // ??????????????????daoAuthenticationProvider
                .apply(loginDaoAuthenticationProviderConfig)
                // ?????????????????????????????????
                .and().apply(smsCodeConfig)


        ;
    }

    /**
     * ??????????????????
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
