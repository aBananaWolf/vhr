package cn.com.security.customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface SecurityContextRepositoryCustomizer {
    void accept(HttpSecurity http);
}
