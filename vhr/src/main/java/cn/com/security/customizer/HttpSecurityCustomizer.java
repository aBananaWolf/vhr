package cn.com.security.customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author wyl
 * @create 2020-08-04 11:17
 */
public interface HttpSecurityCustomizer {
    public void accept(HttpSecurity httpSecurity) throws Exception;
}
