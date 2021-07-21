package cn.com.bo;

import cn.com.entities.*;
import lombok.Data;

/**
 * @author wyl
 * @create 2020-08-08 10:06
 */
@Data
public class EmployeeBO extends EmployeeEntity {
    private Integer workAge;
    private NationEntity nation;
    private PoliticsstatusEntity politicsstatus;
    private DepartmentEntity department;
    private JoblevelEntity jobLevel;
    private PositionEntity position;
    private SalaryEntity salary;
}
