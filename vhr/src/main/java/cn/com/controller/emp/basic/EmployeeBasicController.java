package cn.com.controller.emp.basic;


import cn.com.bo.DepartmentBO;
import cn.com.bo.EmployeeBO;
import cn.com.constant.exception.FailedEnum;
import cn.com.constant.exception.SucceedEnum;
import cn.com.constant.exception.WarnEnum;
import cn.com.entities.*;
import cn.com.exception.ServiceInternalException;
import cn.com.exception.UserIllegalOperationException;
import cn.com.service.*;
import cn.com.util.POIUtils;
import cn.com.vo.RespBean;
import cn.com.vo.RespPageBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@RestController
@RequestMapping("/employee/basic")
public class EmployeeBasicController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private NationService nationService;
    @Autowired
    private PoliticsstatusService politicsstatusService;
    @Autowired
    private JoblevelService jobLevelService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/")
    public RespPageBean getEmployeeByPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, EmployeeEntity employee, Date[] beginDateScope) {
        try {
            if (ArrayUtils.isNotEmpty(beginDateScope) && beginDateScope.length != 2) {
                throw new UserIllegalOperationException(WarnEnum.DATE, (Object) beginDateScope);
            }
            if (ArrayUtils.isEmpty(beginDateScope)) {
                beginDateScope = null;
            }
            return employeeService.getEmployeeByLimit(page, size, employee,beginDateScope);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @Autowired
     private ObjectMapper objectMapper;

    @PostMapping("/")
    public RespBean addEmp(@RequestBody EmployeeEntity employee) {
        try {
            String s = objectMapper.writeValueAsString(employee);
            employeeService.addEmployee(employee);
            return RespBean.ok(SucceedEnum.INSERT);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.INSERT, e);
        }
    }

    @DeleteMapping("/{id}")
    public RespBean deleteEmpByEid(@PathVariable Integer id) {
        try {
            employeeService.removeById(id);
            return RespBean.ok(SucceedEnum.DELETE);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.DELETE, e);
        }
    }

    @PutMapping("/")
    public RespBean updateEmp(@RequestBody EmployeeEntity employee) {
        try {
            employeeService.updateById(employee);
            return RespBean.ok(SucceedEnum.UPDATE);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.UPDATE, e);
        }
    }

    /**
     * 工号采用自定义的8位数模式，前补零
     * @return
     */
    @GetMapping("/maxWorkID")
    public RespBean maxWorkID() {
        try {
            return RespBean.build().setStatus(200)
                    .setObj(String.format("%08d", employeeService.maxWorkID() + 1));
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/nations")
    public List<NationEntity> getAllNations() {
        try {
            return nationService.list();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/politicsstatus")
    public List<PoliticsstatusEntity> getAllPoliticsStatus() {
        try {
            return politicsstatusService.list();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/joblevels")
    public List<JoblevelEntity> getAllJobLevels() {
        try {
            return jobLevelService.list();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/positions")
    public List<PositionEntity> getAllPositions() {
        try {
            return positionService.list();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/deps")
    public List<DepartmentBO> getAllDepartments() {
        try {
            return departmentService.getAllDepartments();
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.SELECT, e);
        }
    }

    @GetMapping("/export")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> exportData() {
        try {
            List<EmployeeBO> data = (List<EmployeeBO>) employeeService.getEmployeeByLimit(1, 2147483647, new EmployeeBO(), null).getData();
            return POIUtils.employee2Excel(data);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.EXCEL_EXPORT, e);
        }
    }

    @PostMapping("/import")
    public RespBean importData(MultipartFile file) {
        try {
            List<NationEntity> nationList = nationService.list();
            List<PoliticsstatusEntity> politicsStatusList = politicsstatusService.list();
            List<DepartmentEntity> departmentList = departmentService.list();
            List<JoblevelEntity> jobLevelList = jobLevelService.list();
            List<PositionEntity> positionList = positionService.list();
            List<EmployeeBO> list = POIUtils.Excel2Employee(file,nationList,politicsStatusList,departmentList,jobLevelList,positionList);
            employeeService.batchAddEmployee(list);
            return RespBean.ok(SucceedEnum.EXCEL_IMPORT);
        } catch (Exception e) {
            throw new ServiceInternalException(FailedEnum.EXCEL_IMPORT, e);
        }
    }
}

