package cn.com.security.provider;

import cn.com.security.service.ImgCodeUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * @author wyl
 * @create 2020-08-03 10:42
 */
@Component
public class LoginDaoAuthenticationProviderConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ImgCodeUserDetailService imgCodeUserDetailService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setPasswordEncoder(encoder);
        provider.setUserDetailsService(imgCodeUserDetailService);
        // 使用我们自己的failureHandler处理抛出异常
        provider.setHideUserNotFoundExceptions(false);

        http.authenticationProvider(provider);
    }
}
