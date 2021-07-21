package cn.com.service;

import cn.com.entities.EmployeeEntity;
import cn.com.vo.RespPageBean;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface EmployeeService extends IService<EmployeeEntity> {

    RespPageBean getEmployeeByLimit(Integer offset, Integer size, EmployeeEntity employee, Date[] beginDateScope);

    void addEmployee(EmployeeEntity employee) throws Exception;

    boolean batchAddEmployee(List<? extends EmployeeEntity> employeeList);

    int maxWorkID();
}
