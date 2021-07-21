package cn.com.controller.system.hr;

import cn.com.bo.HrBO;
import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.entities.HrEntity;
import cn.com.entities.RoleEntity;
import cn.com.exception.ServiceInternalException;
import cn.com.security.session.SessionControlConfig;
import cn.com.service.HrService;
import cn.com.service.RoleService;
import cn.com.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 超级管理员的权限，根据HrId crud
 * @author wyl
 * @create 2020-08-09 19:09
 */
@RestController
@RequestMapping("/system/hr")
public class HrController {
    @Autowired
    HrService hrService;
    @Autowired
    RoleService roleService;

    @GetMapping("/")
    public List<HrBO> getAllHrs(String keywords) {
        try {
            return hrService.getAllHrs(keywords);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    /**
     * {@link SessionControlConfig}
     * @param hr
     * @return
     */
    @PutMapping("/")
    public RespBean updateHr(@RequestBody HrEntity hr) {
        try {
            hrService.updateHr(hr);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @GetMapping("/roles")
    public List<RoleEntity> getAllRoles() {
        try {
            return roleService.list();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @PutMapping("/role")
    public RespBean updateHrRole(Integer hrid, Integer[] rids) {
        try {
            hrService.updateHrRole(hrid, rids);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    @DeleteMapping("/{id}")
    public RespBean deleteHrById(@PathVariable Integer id) {
        try {
            hrService.deleteHrById(id);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }
}
