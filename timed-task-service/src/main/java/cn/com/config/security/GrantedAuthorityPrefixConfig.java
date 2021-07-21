package cn.com.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

/**
 * 额外放置原因同 {@link PasswordEncoderConfig}
 * @author wyl
 * @create 2020-08-06 13:05
 */
@Configuration
public class GrantedAuthorityPrefixConfig {
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // 可以去掉前缀,我的系统已经采用了带前缀的模式
        return new GrantedAuthorityDefaults("ROLE_");
    }
}
