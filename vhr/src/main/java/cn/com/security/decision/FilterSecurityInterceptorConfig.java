package cn.com.security.decision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author wyl
 * @create 2020-08-03 10:26
 */
@Component
public class FilterSecurityInterceptorConfig  {
//    @Autowired
//    private GrantedAuthorityDefaults grantedAuthorityDefaults;
    @Autowired
    private CustomizeAccessDecisionVoter accessDecisionVoter;
    @Bean
    public AccessDecisionManager customizeAccessDecisionManager() {
        // 添加票选者
        ArrayList<AccessDecisionVoter<?>> list = new ArrayList<>();
//        accessDecisionVoter.setGrantedAuthorityDefaults(grantedAuthorityDefaults);
        list.add(accessDecisionVoter);
        return new AffirmativeBased(list);
    }
}
