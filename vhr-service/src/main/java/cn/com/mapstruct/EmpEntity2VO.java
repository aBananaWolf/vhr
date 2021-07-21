package cn.com.mapstruct;

import cn.com.entities.EmployeeEntity;
import cn.com.vo.EmployeeEmailVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmpEntity2VO {
    EmpEntity2VO INSTANCE = Mappers.getMapper(EmpEntity2VO.class);

    EmployeeEmailVO empEntity2VO(EmployeeEntity employeeEntity);
}
