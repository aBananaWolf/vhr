package cn.com.service;

import cn.com.bo.DepartmentBO;
import cn.com.entities.DepartmentEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface DepartmentService extends IService<DepartmentEntity> {

    List<DepartmentBO> getAllDepartments();

    void insertDepartment(DepartmentBO dep);

    void deleteDepartmentById(DepartmentBO dep);
}
