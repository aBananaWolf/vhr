package cn.com.security.decision;

import cn.com.bo.MenuBO;
import cn.com.util.MyUrlUtils;
import cn.com.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * 和 AccessDecisionVoter 协定好，ROLE_login 角色的意味着：它是非数据库定义的资源路径、并且数据库中没有该角色，如果用户没有登录则无法访问
 * @author wyl
 * @create 2020-08-03 10:16
 */
@Component
public class CustomizeSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    @Autowired
    private GrantedAuthorityDefaults grantedAuthorityDefaults;
    @Autowired
    private MenuService menuService;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (object instanceof FilterInvocation) {
            FilterInvocation filterInvocation = (FilterInvocation) object;
            String actualUri = MyUrlUtils.getActualUrl(filterInvocation.getRequest());
            // 缓存
            List<MenuBO> menuList = menuService.listAllMenuAndRole();
            for (MenuBO menu : menuList) {
                // 获取匹配路径所需角色
                if (antPathMatcher.match(menu.getUrl(),actualUri) && !CollectionUtils.isEmpty(menu.getRoles())) {
                    String[] arr = new String[menu.getRoles().size()];
                    for (int i = 0; i < menu.getRoles().size(); i++) {
                        arr[i] = menu.getRoles().get(i).getName();
                    }
                    return SecurityConfig.createList(arr);
                }
            }
            return SecurityConfig.createList(grantedAuthorityDefaults.getRolePrefix() + "login");
        }
        // 这是基本上到不了的
        throw new AuthorizationServiceException("没有权限");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
