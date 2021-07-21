package cn.com.mq.config;

import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.mq.common.AbstractVhrRabbitTemplate;
import cn.com.mq.sender.ErrorEMailSender;
import cn.com.service.SmsVerificationCodeSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author wyl
 * @create 2020-08-17 15:40
 */
@Slf4j
@Configuration
public class SmsVerificationCodeRabbitTemplateConfig {
    @Autowired
    private AbstractVhrRabbitTemplate abstractVhrRabbitTemplate;
    @Autowired
    private SmsVerificationCodeSendLogService smsVerificationCodeSendLogService;
    @Autowired
    private ErrorEMailSender errorEMailSender;

    private final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if (ack) {
                SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity = new SmsVerificationCodeSendLogEntity();
                smsVerificationCodeSendLogEntity.setUpdateTime(LocalDateTime.now());
                smsVerificationCodeSendLogEntity.setStatus(MessageConstants.SENDING);
                String correlationId = correlationData.getId();
                smsVerificationCodeSendLogEntity.setMsgId(correlationId);
                try {
                    // 异常测试
//                    int a = 1 / 0;
                    int affected = smsVerificationCodeSendLogService.idempotentInConsumers(smsVerificationCodeSendLogEntity, MessageConstants.SENT_SUCCESSFUL);
                    if (affected == 1) {
                        if (log.isInfoEnabled()) {
                            log.info("rabbitmq成功发送了一条消息：" + correlationData.getId());
                        }
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("rabbitmq发送了一条失败的消息，处理结果决定于returnedCallback" + correlationData.getId());
                        }
                    }
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库操作失败，等待定时任务", e);
                    }
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error("rabbitmq nack " + correlationData.getId() + "\t" + cause);
                }
            }
        }
    };

    @SuppressWarnings("all")
    @Bean("smsVerificationCodeTemplate")
    public RabbitTemplate rabbitTemplate() throws Exception {
        RabbitTemplate rabbitTemplate = abstractVhrRabbitTemplate.getObject();
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity = new SmsVerificationCodeSendLogEntity();
                smsVerificationCodeSendLogEntity.setUpdateTime(LocalDateTime.now());
                smsVerificationCodeSendLogEntity.setStatus(MessageConstants.SENDING);
                smsVerificationCodeSendLogEntity.setExchange(exchange);
                smsVerificationCodeSendLogEntity.setRoutingKey(routingKey);
                String correlationId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
                // 修改为失败状态
                try {
                    smsVerificationCodeSendLogService.idempotentInConsumers(smsVerificationCodeSendLogEntity, MessageConstants.FAIL_IN_SEND);
                    if (log.isErrorEnabled()) {
                        log.error("成功将无法路由的消息持久化进数据库：" + correlationId);
                    }
                    // 邮件
                    ErrorMailSendLogEntity errorMailSendLogEntity = new ErrorMailSendLogEntity();
                    errorMailSendLogEntity.setBody("将无法路由的消息持久化进数据库：" + correlationId);
                    errorEMailSender.sendErrorEMailAndSaveLog(errorMailSendLogEntity);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("无法路由的消息不能持久化进入数据库，出现异常：", e);
                    }
                }
            }
        });
        rabbitTemplate.setConfirmCallback(confirmCallback);
        return rabbitTemplate;
    }
}
