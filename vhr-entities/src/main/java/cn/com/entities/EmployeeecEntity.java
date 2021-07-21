package cn.com.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@TableName("employeeec")
public class EmployeeecEntity implements Serializable {

    private static final long serialVersionUID=1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 员工编号
     */
    private Integer eid;

    /**
     * 奖罚日期
     */
    @TableField("ecDate")
    private LocalDate ecDate;

    /**
     * 奖罚原因
     */
    @TableField("ecReason")
    private String ecReason;

    /**
     * 奖罚分
     */
    @TableField("ecPoint")
    private Integer ecPoint;

    /**
     * 奖罚类别，0：奖，1：罚
     */
    @TableField("ecType")
    private Integer ecType;

    /**
     * 备注
     */
    private String remark;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public LocalDate getEcDate() {
        return ecDate;
    }

    public void setEcDate(LocalDate ecDate) {
        this.ecDate = ecDate;
    }

    public String getEcReason() {
        return ecReason;
    }

    public void setEcReason(String ecReason) {
        this.ecReason = ecReason;
    }

    public Integer getEcPoint() {
        return ecPoint;
    }

    public void setEcPoint(Integer ecPoint) {
        this.ecPoint = ecPoint;
    }

    public Integer getEcType() {
        return ecType;
    }

    public void setEcType(Integer ecType) {
        this.ecType = ecType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "EmployeeecEntity{" +
        "id=" + id +
        ", eid=" + eid +
        ", ecDate=" + ecDate +
        ", ecReason=" + ecReason +
        ", ecPoint=" + ecPoint +
        ", ecType=" + ecType +
        ", remark=" + remark +
        "}";
    }
}
