package cn.com.service.impl;

import cn.com.dao.MenuRoleDao;
import cn.com.entities.MenuRoleEntity;
import cn.com.service.MenuRoleService;
import cn.com.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@Service
public class MenuRoleServiceImpl extends ServiceImpl<MenuRoleDao, MenuRoleEntity> implements MenuRoleService {

    @Autowired
    private MenuRoleDao menuRoleDao;
    @Autowired
    private MenuService menuService;

    @Override
    @Transactional

    @CacheEvict(cacheNames = MenuServiceImpl.RESOURCE_MENU_CACHE_PREFIX)
//    @CacheFlag(cachePrefix = MenuServiceImpl.RESOURCE_MENU_CACHE_PREFIX,lockPrefix = MenuServiceImpl.RESOURCE_MENU_LOCK, cacheType = CacheTypeEnum.WRITE)
    public void updateMenuRole(Integer rid, Integer[] mids) {
        MenuRoleEntity menuRoleDelEntity = new MenuRoleEntity();
        menuRoleDelEntity.setRid(rid);
        QueryWrapper<MenuRoleEntity> delWrapper = new QueryWrapper<>(menuRoleDelEntity);
        menuRoleDao.delete(delWrapper);

        ArrayList<MenuRoleEntity> list = new ArrayList<>(mids.length);
        for (int i = 0; i < mids.length; i++) {
            MenuRoleEntity menuRole = new MenuRoleEntity();
            menuRole.setRid(rid);
            menuRole.setMid(mids[i]);
            list.add(menuRole);
        }
        menuRoleDao.batchSave(list);
        // 可以做把所有影响到的hr强制下线，但没有必要，下次登录就会发现redis的数据被刷新了
    }

    @Override
    public List<Integer> getMenuIdsByRid(Integer rid) {
        return menuRoleDao.selectMenuIdsByRid(rid);
    }
}
