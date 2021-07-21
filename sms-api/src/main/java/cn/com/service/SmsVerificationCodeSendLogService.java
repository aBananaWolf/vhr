package cn.com.service;

import cn.com.entities.SmsVerificationCodeSendLogEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyl
 * @since 2020-08-17
 */
public interface SmsVerificationCodeSendLogService extends IService<SmsVerificationCodeSendLogEntity> {

    int idempotentInConsumers(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, Integer clientReceivedStatus);

    List<SmsVerificationCodeSendLogEntity> findMessageByStatusAndTryTime(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, Integer page, Integer size);
}
