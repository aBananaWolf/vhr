package cn.com.properties;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author wyl
 * @create 2020-08-05 10:48
 */
@Getter
@Configuration
public class GlobalEnvironment {
    private String profile;

    public static final String DEV = "dev";
    public static final String PROD = "PROD";

    @Bean
    @Profile("dev")
    protected void flagDev() {
        profile = DEV;
    }

    @Bean
    @Profile("prod")
    protected void flagProd() {
        profile = PROD;
    }
}
