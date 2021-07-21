package cn.com.mapstruct;

import cn.com.entities.MenuEntity;
import cn.com.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author wyl
 * @create 2020-08-06 11:41
 */
@Mapper
public interface MenuEntity2VO {
    MenuEntity2VO INSTANCE = Mappers.getMapper(MenuEntity2VO.class);

    MenuVO menuEntity2VO(MenuEntity menuEntity);
}
