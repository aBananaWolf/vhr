package cn.com.security.config;

import cn.com.security.constant.SecurityConstant;
import cn.com.security.customizer.HttpSecurityCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author wyl
 * @create 2020-08-02 11:27
 */
@Configuration
@Order(320)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private HttpSecurityCustomizer customizer;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(SecurityConstant.IGNORE_URLS);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        customizer.accept(http);
    }
}
