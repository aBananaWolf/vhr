package cn.com.dao;

import cn.com.bo.HrBO;
import cn.com.entities.HrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-02
 */
public interface HrDao extends BaseMapper<HrEntity> {

    List<HrBO> listHrAndRoleByHrBO(HrBO hr);

    List<HrBO> selectAllHrsWithRole(HrEntity hrEntity);
}
