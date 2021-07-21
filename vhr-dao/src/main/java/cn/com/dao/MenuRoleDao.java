package cn.com.dao;

import cn.com.entities.MenuRoleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface MenuRoleDao extends BaseMapper<MenuRoleEntity> {

    void batchSave(ArrayList<MenuRoleEntity> list);

    List<Integer> selectMenuIdsByRid(Integer rid);
}
