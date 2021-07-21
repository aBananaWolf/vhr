package cn.com.controller.system.basic;


import cn.com.bo.DepartmentBO;
import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.exception.ServiceInternalException;
import cn.com.service.DepartmentService;
import cn.com.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 这里写了两条存储过程
 * @author wyl
 * @since 2020-08-02
 */
@RestController
@RequestMapping("/system/basic/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/")
    public List<DepartmentBO> getAllDepartments() {
        try {
            return departmentService.getAllDepartments();
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }
    @PostMapping("/")
    public RespBean addDep(@RequestBody DepartmentBO dep) {
        try {
            departmentService.insertDepartment(dep);
            return RespBean.ok(SucceedEnum.INSERT.getTip(), dep);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.INSERT, e);
        }
    }
    @DeleteMapping("/{id}")
    public RespBean deleteDepById(@PathVariable Integer id) {
        try {
            if (id == null) {
                throw new ServiceInternalException(FailedEnum.DELETE, id);
            }
            DepartmentBO dep = new DepartmentBO();
            dep.setId(id);
            departmentService.deleteDepartmentById(dep);
            if (dep.getAffectedRows() == null) {
                return RespBean.error("删除失败");
            } else if (dep.getAffectedRows() == -1) {
                return RespBean.error("该部门下仍有 "+ dep.getExtensionFlag() +" 位员工，删除失败");
            } else if (dep.getAffectedRows() == -2) {
                return RespBean.error("该部门下仍有 " + dep.getExtensionFlag() + " 个子部门，删除失败");
            }  else if (dep.getAffectedRows() == 1) {
                return RespBean.ok("删除成功");
            }
            return RespBean.ok(SucceedEnum.DELETE);
        } catch(Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }
}

