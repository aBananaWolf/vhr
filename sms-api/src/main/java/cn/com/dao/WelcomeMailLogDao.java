package cn.com.dao;

import cn.com.entities.WelcomeMailLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-18
 */
public interface WelcomeMailLogDao extends BaseMapper<WelcomeMailLogEntity> {

    int idempotentInConsumers(@Param("welcomeEntity") WelcomeMailLogEntity welcomeMailLogEntity,
                              @Param("clientReceivedStatus")Integer clientReceivedStatus);

    List<WelcomeMailLogEntity> findMessageByStatusAndTryTime(
            @Param("welcomeEntity")WelcomeMailLogEntity welcomeMailLogEntity,
            @Param("page")Integer page,
            @Param("size")Integer size);
}
