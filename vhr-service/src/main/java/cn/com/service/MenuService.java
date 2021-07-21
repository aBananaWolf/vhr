package cn.com.service;

import cn.com.bo.MenuBO;
import cn.com.entities.MenuEntity;
import cn.com.vo.MenuVO;
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
public interface MenuService extends IService<MenuEntity> {

    List<MenuBO> listAllMenuAndRole();

    List<MenuVO> listMenuByHr(List<String> roles);


    List<MenuVO> getMenusByRoot();

}
