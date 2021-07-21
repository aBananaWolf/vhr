package cn.com.service.impl;

import cn.com.anno.CacheFlag;
import cn.com.constant.cache.CacheTypeEnum;
import cn.com.dao.HrRoleDao;
import cn.com.dao.MenuRoleDao;
import cn.com.dao.RoleDao;
import cn.com.entities.HrRoleEntity;
import cn.com.entities.MenuRoleEntity;
import cn.com.entities.RoleEntity;
import cn.com.exception.UserIllegalOperationException;
import cn.com.service.MenuService;
import cn.com.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleDao, RoleEntity> implements RoleService {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private HrRoleDao hrRoleDao;
    @Autowired
    private MenuRoleDao menuRoleDao;
    @Autowired
    private MenuService menuService;


    /**
     * 删除一个角色，角色表是没有外键约束的，要手动判断
     * 1.查询HrRole确认无引用
     * 2.删除MenuRole引用
     * 3.删除角色
     */
    @Override
    @Transactional
    @CacheFlag(cachePrefix = MenuServiceImpl.RESOURCE_MENU_CACHE_PREFIX,lockPrefix = MenuServiceImpl.RESOURCE_MENU_LOCK, cacheType = CacheTypeEnum.WRITE)
    public void deleteRoleById(Integer rid) {
        // 1
        HrRoleEntity hrRoleEntity = new HrRoleEntity();
        hrRoleEntity.setRid(rid);
        QueryWrapper<HrRoleEntity> checkWrapper = new QueryWrapper<>(hrRoleEntity);
        Integer count = hrRoleDao.selectCount(checkWrapper);
        if (count != null && count > 0) {
            throw new UserIllegalOperationException("该角色仍有 " + count + " 条引用！暂无法删除");
        }
        // == null || <= 0 continue

        // 2
        MenuRoleEntity menuRoleEntity = new MenuRoleEntity();
        menuRoleEntity.setRid(rid);
        QueryWrapper<MenuRoleEntity> delWrapper = new QueryWrapper<>(menuRoleEntity);
        menuRoleDao.delete(delWrapper);

        // 3
        roleDao.deleteById(rid);
    }


 /*   @Override
    @CacheFlag(cachePrefix = MenuServiceImpl.RESOURCE_MENU_CACHE_PREFIX,lockPrefix = MenuServiceImpl.RESOURCE_MENU_LOCK, cacheType = CacheTypeEnum.WRITE)
    public boolean save(RoleEntity entity) {
        roleDao.insert(entity);
        menuService.listAllMenuAndRole();
        return true;
    }*/
}
