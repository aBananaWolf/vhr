package cn.com.mq.config;

import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.mq.common.AbstractVhrRabbitTemplate;
import cn.com.mq.sender.ErrorEMailSender;
import cn.com.service.WelcomeMailLogService;
import com.rabbitmq.client.ReturnListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wyl
 * @create 2020-08-18 14:29
 */
@Slf4j
@Configuration
public class WelcomeMailRabbitTemplateConfig {
    @Autowired
    private AbstractVhrRabbitTemplate abstractVhrRabbitTemplate;
    @Autowired
    private WelcomeMailLogService welcomeMailLogService;
    @Autowired
    private ErrorEMailSender errorEMailSender;

    private RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if (ack) {
                WelcomeMailLogEntity welcomeMailLogEntity = new WelcomeMailLogEntity();
                welcomeMailLogEntity.setMsgId(correlationData.getId());
                welcomeMailLogEntity.setStatus(MessageConstants.SENDING);
                welcomeMailLogEntity.setUpdateTime(LocalDateTime.now());
                try {
                    // 异常测试
//                    int a = 1 / 0;
                    int affected = welcomeMailLogService.idempotentInConsumers(welcomeMailLogEntity, MessageConstants.SENT_SUCCESSFUL);
                    if (affected == 1) {
                        if (log.isInfoEnabled()) {
                            log.info("rabbitmq成功的发出了一条消息：" + correlationData.getId());
                        }
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("rabbitmq发送了一条无法路由的消息，处理结果决定于returnCallback");
                        }
                    }
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库操作失败，等待定时任务", e);
                    }
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("消息发送失败，等待分布式定时任务");
                }
            }
        }
    };

    @SuppressWarnings("all")
    @Bean("welcomeMailRabbitTemplate")
    public RabbitTemplate rabbitTemplate() throws Exception {

        RabbitTemplate rabbitTemplate = abstractVhrRabbitTemplate.getObject();
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                WelcomeMailLogEntity welcomeMailLogEntity = new WelcomeMailLogEntity();
                String correlationId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
                welcomeMailLogEntity.setMsgId(correlationId);
                welcomeMailLogEntity.setRoutingKey(routingKey);
                welcomeMailLogEntity.setExchange(exchange);
                welcomeMailLogEntity.setUpdateTime(LocalDateTime.now());
                welcomeMailLogEntity.setStatus(MessageConstants.SENDING);
                try {
                    welcomeMailLogService.idempotentInConsumers(welcomeMailLogEntity, MessageConstants.FAIL_IN_SEND);
                    if (log.isErrorEnabled()) {
                        log.error("成功将无法路由的消息持久化进数据库：" + correlationId);
                    }
                    ErrorMailSendLogEntity errorMailSendLogEntity = new ErrorMailSendLogEntity();
                    errorMailSendLogEntity.setBody("成功将无法路由的消息持久化进数据库：" + correlationId);
                    errorEMailSender.sendErrorEMailAndSaveLog(errorMailSendLogEntity);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("无法路由的消息不能持久化进数据库，出现异常：", e);
                    }
                }
            }
        });
        return rabbitTemplate;
    }
}
