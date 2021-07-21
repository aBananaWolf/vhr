package cn.com.security.entrypoint;

import cn.com.security.constant.SecurityConstant;
import cn.com.security.handler.LoginFailureHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * @author wyl
 * @create 2020-08-21 10:10
 */
@Slf4j
@Configuration
public class AuthenticationEntryPointConfig {
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        AuthenticationEntryPoint authenticationEntryPoint = new AuthenticationEntryPoint(SecurityConstant.LOGIN_ENTRY_POINT);
        authenticationEntryPoint.setLoginFailureHandler(loginFailureHandler);
        return authenticationEntryPoint;
    }
}
