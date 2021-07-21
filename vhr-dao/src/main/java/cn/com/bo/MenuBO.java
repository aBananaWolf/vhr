package cn.com.bo;

import cn.com.entities.MenuEntity;
import cn.com.entities.RoleEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wyl
 * @create 2020-08-03 13:38
 */
@Data
public class MenuBO extends MenuEntity implements Serializable {
    private List<RoleEntity> roles;
}
