package cn.com.dao;

import cn.com.bo.DepartmentBO;
import cn.com.entities.DepartmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {

    List<DepartmentBO> selectAllDepartments(Integer id);

    void insertDepartment(DepartmentBO dep);

    void deleteDepartmentById(DepartmentBO dep);
}
