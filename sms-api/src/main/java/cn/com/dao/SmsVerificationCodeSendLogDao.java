package cn.com.dao;

import cn.com.entities.SmsVerificationCodeSendLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyl
 * @since 2020-08-17
 */
public interface SmsVerificationCodeSendLogDao extends BaseMapper<SmsVerificationCodeSendLogEntity> {

    int idempotentInConsumers(@Param("smsEntity") SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity,
                              @Param("clientReceivedStatus") Integer clientReceivedStatus);

    List<SmsVerificationCodeSendLogEntity> findMessageByStatusAndTryTime(
            @Param("smsEntity") SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity,
            @Param("page")Integer page,
            @Param("size")Integer size);
}
