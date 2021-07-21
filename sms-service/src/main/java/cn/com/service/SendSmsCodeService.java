package cn.com.service;

import cn.com.entities.SmsVerificationCodeSendLogEntity;

public interface SendSmsCodeService {
    void sendSmsCode(SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity, int clientReceivedIt) throws Exception;
}
