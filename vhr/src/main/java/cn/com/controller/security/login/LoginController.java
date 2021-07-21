package cn.com.controller.security.login;

import cn.com.bo.Hr;
import cn.com.constant.exception.FailedEnum;
import cn.com.exception.ServiceInternalException;
import cn.com.service.MenuService;
import cn.com.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author wyl
 * @create 2020-08-03 14:19
 */
@RestController
public class LoginController {
    @Autowired
    private MenuService menuService;

    @RequestMapping("/system/config/menu")
    /**
     * 提供页面菜单栏的必要数据，每个子分类都要带上父分类id
     */
    public List<MenuVO> listHrMenu(Authentication authentication) {
        try {
            Collection<? extends GrantedAuthority> authorities = ((Hr) authentication.getPrincipal()).getAuthorities();
            List<String> roles = authorities.stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(toList());
            return menuService.listMenuByHr(roles);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }
}
