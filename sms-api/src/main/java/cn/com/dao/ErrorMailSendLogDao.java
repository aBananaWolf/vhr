package cn.com.dao;

import cn.com.entities.ErrorMailSendLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-14
 */
public interface ErrorMailSendLogDao extends BaseMapper<ErrorMailSendLogEntity> {

    int idempotentInConsumers(@Param("errorMail") ErrorMailSendLogEntity errorMailSendLogUDPEntity, @Param("clientReceivedStatus") Integer clientReceivedStatus);

    List<ErrorMailSendLogEntity> selectMessageByStatusAndTryTime(@Param("errorMail") ErrorMailSendLogEntity errorMailSendLogEntity,
                                                                 @Param("pageNum") Integer page,
                                                                 @Param("pageSize") Integer size);
}
