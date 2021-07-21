package cn.com.security.decision;

import cn.com.security.entrypoint.AuthenticationEntryPoint;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * security 控制授权的一个类，投票ACCESS_DENIED 即没有权限，匿名用户需要登录
 * @author wyl
 * @create 2020-08-03 10:57
 */
@Component
public class CustomizeAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation> {
    @Autowired
    private GrantedAuthorityDefaults grantedAuthorityDefaults;

    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();


    public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> attributes) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (CollectionUtils.isEmpty(attributes))
            return AccessDecisionVoter.ACCESS_DENIED;
        /**
         * 让没有登录的错误请求进入授权点？(要重定向...)
         * 让没有登录的恶意请求进入授权点？(不能重定向...)
         * {@link AuthenticationEntryPoint}
         */
        for (ConfigAttribute attribute : attributes) {
            // 匿名用户需要登录
            if (StringUtils.equals(attribute.getAttribute(),grantedAuthorityDefaults.getRolePrefix() + "login")) {
                if (trustResolver.isAnonymous(authentication)) {
                    // 没有登录的请求最终走入授权点
                    return AccessDecisionVoter.ACCESS_DENIED;
                }
                // 不是匿名用户说明没有这个路径的授权配置
                // 1：error 或者 非项目资源路径
                // 2：没有授权配置的资源路径，但确实有这个资源
                // 选择放行，错误的路径走入全局错误管理，正确的路径走入资源路径
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            for (GrantedAuthority authority : authorities) {
                // 用户拥有admin角色放行、或者匹配放行
                if (StringUtils.equals(authority.getAuthority(), grantedAuthorityDefaults.getRolePrefix() + "admin") || StringUtils.equals(attribute.getAttribute(),authority.getAuthority())) {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }
        }

        return AccessDecisionVoter.ACCESS_DENIED;
    }


    public boolean supports(ConfigAttribute attribute) {
        return true;
    }


    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
