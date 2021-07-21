package cn.com.service;

import cn.com.entities.MenuRoleEntity;
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
public interface MenuRoleService extends IService<MenuRoleEntity> {

    void updateMenuRole(Integer rid, Integer[] mids);

    List<Integer> getMenuIdsByRid(Integer rid);
}
