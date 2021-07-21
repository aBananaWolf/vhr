package cn.com.dao;

import cn.com.entities.EmployeeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {

    List<List<?>> getEmployeeByLimit(@Param("offset") Integer offset,
                                     @Param("size") Integer size,
                                     @Param("emp") EmployeeEntity employee,
                                     @Param("dateArray") String[] dateArray);

    int maxWorkId();

    void batchInsertEmployee(@Param("empList")List<? extends EmployeeEntity> employeeList);
}
