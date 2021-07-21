package cn.com.dao;

import cn.com.bo.MenuBO;
import cn.com.entities.MenuEntity;
import cn.com.vo.MenuVO;
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
public interface MenuDao extends BaseMapper<MenuEntity> {

    List<MenuBO> listAllMenuAndRole();

    List<MenuVO> listMenuByHr(@Param("roles") List<String> roles);
}
