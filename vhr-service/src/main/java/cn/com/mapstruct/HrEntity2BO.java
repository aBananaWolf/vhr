package cn.com.mapstruct;

import cn.com.bo.Hr;
import cn.com.entities.HrEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author wyl
 * @create 2020-08-05 15:54
 */
@Mapper
public interface HrEntity2BO {
    HrEntity2BO INSTANCE = Mappers.getMapper(HrEntity2BO.class);

    Hr hrEntity2BO(HrEntity hrEntity);
}
