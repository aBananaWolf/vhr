package cn.com.security.customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface SessionControlCustomizer {
    void accept(HttpSecurity http) throws Exception;
}
