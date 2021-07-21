package cn.com.bo;

import cn.com.entities.DepartmentEntity;
import lombok.Data;

import java.util.List;

/**
 * @author wyl
 * @create 2020-08-06 10:30
 */
@Data
public class DepartmentBO extends DepartmentEntity {
    private List<DepartmentBO> children;

    // 可以是自定义的标志位，由存储过程和controller 一起商定
    private Integer affectedRows;

    // 同上
    private Integer extensionFlag;
}
