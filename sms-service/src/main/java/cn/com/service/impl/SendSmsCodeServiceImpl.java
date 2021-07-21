package cn.com.service.impl;

import cn.com.constant.message.MessageConstants;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.service.SendSmsCodeService;
import cn.com.service.SmsVerificationCodeSendLogService;
import cn.com.utils.SmsCodeSenderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wyl
 * @create 2020-08-17 20:57
 */
@Component
public class SendSmsCodeServiceImpl implements SendSmsCodeService {
    @Autowired
    private SmsVerificationCodeSendLogService smsVerificationCodeSendLogService;

    @Override
    @Transactional(timeout = -1)
    public void sendSmsCode(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, int clientReceivedIt) throws Exception {
        int affected = smsVerificationCodeSendLogService.idempotentInConsumers(smsVerificationCodeSendLogEntity, MessageConstants.CONSUME_SUCCESSFUL);
        if (affected != 1) {
            return;
        }
        SmsCodeSenderUtils.sendSmsCode(smsVerificationCodeSendLogEntity.getCode(), String.valueOf(smsVerificationCodeSendLogEntity.getPhone()));
    }
}
