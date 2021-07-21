package cn.com.controller.system.basic;

import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.entities.RoleEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.service.MenuRoleService;
import cn.com.service.MenuService;
import cn.com.service.RoleService;
import cn.com.vo.MenuVO;
import cn.com.vo.RespBean;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wyl
 * @create 2020-08-06 11:42
 */
@RestController
@RequestMapping("/system/basic/permiss")
public class PermitsController {
    @Autowired
    private GrantedAuthorityDefaults grantedAuthorityDefaults;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private MenuRoleService menuRoleService;

    @GetMapping("/")
    public List<RoleEntity> getAllRoles() {
        try {
            return roleService.list();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/menus")
    public List<MenuVO> getAllMenus() {
        try {
            return menuService.getMenusByRoot();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/mids/{rid}")
    public List<Integer> getMenuIdsByRid(@PathVariable Integer rid) {
        try {
            return menuRoleService.getMenuIdsByRid(rid);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @PutMapping("/")
    public RespBean updateMenuRole(Integer rid, Integer[] mids) {
        try {
            if (rid == null || ArrayUtils.isEmpty(mids)) {
                throw new ServiceInternalException(FailedEnum.DEFAULT, rid, mids);
            }
            menuRoleService.updateMenuRole(rid,mids);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @PostMapping("/role")
    public RespBean addRole(@RequestBody RoleEntity role) {
        role.setName(grantedAuthorityDefaults.getRolePrefix() + role.getName());
        try {
            // 添加一个只有名字的角色，单表
            roleService.save(role);
            return RespBean.ok(SucceedEnum.INSERT);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.INSERT, e);
        }
    }

    @DeleteMapping("/role/{rid}")
    public RespBean deleteRoleById(@PathVariable Integer rid) {
        try {
            roleService.deleteRoleById(rid);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }
}
