package cn.com.mq.sender;

import cn.com.constant.message.MessageConstants;
import cn.com.constant.message.SmsCodeConstants;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.service.SmsVerificationCodeSendLogService;
import cn.com.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author wyl
 * @create 2020-08-17 16:59
 */
@Slf4j
@Component
public class SmsCodeSender {
    @Autowired
    @Qualifier("smsVerificationCodeTemplate")
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SmsVerificationCodeSendLogService smsVerificationCodeSendLogService;

    public void sendSmsVerificationCode(String phone, String code, String correlationId) {
        rabbitTemplate.send(
                SmsCodeConstants.SMS_CODE_EXCHANGE,
                // 测试returnedListener
//                SmsCodeConstants.SMS_CODE_ROUTING_KEY + "x",
                SmsCodeConstants.SMS_CODE_ROUTING_KEY,
                MessageBuilder.withBody((phone + ":" + code).getBytes())
                    .andProperties(
                            MessagePropertiesBuilder.newInstance()
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                                .setContentEncoding("UTF-8")
                                .setContentType("text/plain")
                                .build()
                    ).build(),
                new CorrelationData(correlationId)
        );
    }

    public void sendSmsVerificationCodeAndSaveLog(SmsVerificationCodeSendLogEntity entity) throws Exception {
        // 交换机等属性
        entity.setExchange(SmsCodeConstants.SMS_CODE_EXCHANGE);
        entity.setRoutingKey(SmsCodeConstants.SMS_CODE_ROUTING_KEY);
        entity.setAttemptsCount(0);
        entity.setStatus(MessageConstants.SENDING);
        // 时间
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        // 短信验证码发送的前五秒都很正常
        entity.setTryTime(now.plusSeconds(SmsCodeConstants.SMS_CODE_SENT_DELAY_TIME));
        entity.setUpdateTime(now);
        // 唯一id correlationId
        String correlationId = IdGenerator.getId();
        entity.setMsgId(correlationId);
        // 不捕获异常，抛出，这是可以让用户直接重试的
        smsVerificationCodeSendLogService.save(entity);
        try {
            this.sendSmsVerificationCode(String.valueOf(entity.getPhone()),entity.getCode(),correlationId);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("errorMail发送失败，可能是rabbitmq出现异常", e);
            }
        }
    }

    // 定时任务专用
    @Transactional(timeout = -1)
    public void sendSmsVerificationCodeAndUpdateLog(SmsVerificationCodeSendLogEntity verificationCodeSendLogEntity) {
        // 一般不可能为空
        Long phone = verificationCodeSendLogEntity.getPhone();
        if (phone == null) {
            return;
        }
        verificationCodeSendLogEntity.setStatus(MessageConstants.SENDING);
        int affected = smsVerificationCodeSendLogService.idempotentInConsumers(verificationCodeSendLogEntity, MessageConstants.SENT_SUCCESSFUL);
        if (affected != 1) {
            return;
        }

        this.sendSmsVerificationCode(String.valueOf(phone),verificationCodeSendLogEntity.getCode(), verificationCodeSendLogEntity.getMsgId());
    }
}
