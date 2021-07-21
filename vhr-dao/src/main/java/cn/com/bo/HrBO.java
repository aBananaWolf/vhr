package cn.com.bo;

import cn.com.entities.HrEntity;
import cn.com.entities.RoleEntity;
import lombok.Data;

import java.util.List;

/**
 * 带角色的 HrEntity
 * @author wyl
 * @create 2020-08-05 16:31
 */
@Data
public class HrBO extends HrEntity {
    private static final long serialVersionUID = 9186144693825815253L;
    private List<RoleEntity> roles;
}
