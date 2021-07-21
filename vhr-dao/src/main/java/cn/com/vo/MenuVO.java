package cn.com.vo;

import cn.com.entities.MenuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author wyl
 * @create 2020-08-03 19:16
 */
@Data
public class MenuVO extends MenuEntity {
    private List<MenuVO> children;
}
