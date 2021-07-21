package cn.com.dao;

import cn.com.entities.HrRoleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface HrRoleDao extends BaseMapper<HrRoleEntity> {

    void bachSave(@Param("hrRole") ArrayList<HrRoleEntity> list);
}
