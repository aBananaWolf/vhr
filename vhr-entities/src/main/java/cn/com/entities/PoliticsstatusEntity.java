package cn.com.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
@TableName("politicsstatus")
public class PoliticsstatusEntity implements Serializable {

    private static final long serialVersionUID=1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    public PoliticsstatusEntity(String name) {
        this.name = name;
    }

    public PoliticsstatusEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PoliticsstatusEntity{" +
        "id=" + id +
        ", name=" + name +
        "}";
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        else if (obj == null || obj.getClass() != this.getClass()) return false;
        else return ((PoliticsstatusEntity)obj).getName().equals(this.name);
    }
}
