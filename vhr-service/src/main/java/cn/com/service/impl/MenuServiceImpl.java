package cn.com.service.impl;

import cn.com.bo.MenuBO;
import cn.com.dao.MenuDao;
import cn.com.entities.MenuEntity;
import cn.com.mapstruct.MenuEntity2VO;
import cn.com.service.MenuService;
import cn.com.vo.MenuVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
public class MenuServiceImpl extends ServiceImpl<MenuDao, MenuEntity> implements MenuService {

    public static final String RESOURCE_MENU_LOCK = "lock:menuList:all";
    public static final String RESOURCE_MENU_CACHE_PREFIX = "vhr:menuList:all";

    @Autowired
    private MenuDao menuDao;
    @Autowired
    private GrantedAuthorityDefaults grantedAuthorityDefaults;

    /**
     * 读取的频率较高
     * @return
     */
    @Cacheable(cacheNames = RESOURCE_MENU_CACHE_PREFIX)
//    @CacheFlag(cachePrefix = RESOURCE_MENU_CACHE_PREFIX,lockPrefix = RESOURCE_MENU_LOCK, cacheType = CacheTypeEnum.READ)
    public List<MenuBO> listAllMenuAndRole() {
        return menuDao.listAllMenuAndRole();
    }

    @Override
    public List<MenuVO> listMenuByHr(List<String> roles) {
        return menuDao.listMenuByHr(roles);
    }

    @Override
    public List<MenuVO> getMenusByRoot() {
        // 直接获取admin权限的菜单
        List<MenuVO> menuVOList = listMenuByHr(Collections.singletonList(grantedAuthorityDefaults.getRolePrefix() + "admin"));
        // 包装一层根即可
        MenuEntity menuEntity = menuDao.selectById(1);
        MenuVO menuVO = MenuEntity2VO.INSTANCE.menuEntity2VO(menuEntity);
        menuVO.setChildren(menuVOList);
        return Collections.singletonList(menuVO);
    }
}
