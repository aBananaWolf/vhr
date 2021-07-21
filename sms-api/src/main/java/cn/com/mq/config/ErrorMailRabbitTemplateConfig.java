package cn.com.mq.config;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.mq.common.AbstractVhrRabbitTemplate;
import cn.com.mq.sender.ErrorEMailSender;
import cn.com.mq.threadpool.MessageScheduleThreadPool;
import cn.com.service.ErrorMailSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

/**
 * 保障消息投递可靠性
 * @author wyl
 * @create 2020-08-14 11:14
 */
@Slf4j
@Configuration
public class ErrorMailRabbitTemplateConfig {
    @Autowired
    private AbstractVhrRabbitTemplate abstractVhrRabbitTemplate;
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;

    private final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if (ack) {
                // 修改为发送成功
                ErrorMailSendLogEntity errorMailSendLogUDPEntity = new ErrorMailSendLogEntity();
                errorMailSendLogUDPEntity.setStatus(MessageConstants.SENDING);
                errorMailSendLogUDPEntity.setUpdateTime(LocalDateTime.now());
                errorMailSendLogUDPEntity.setMsgId(correlationData.getId());
                try {
                    int affected = errorMailSendLogService.idempotentInConsumers(errorMailSendLogUDPEntity, MessageConstants.SENT_SUCCESSFUL);
                    if (affected == 1) {
                        if (log.isInfoEnabled()) {
                            log.info("rabbitmq成功发送了一条消息" + correlationData.getId());
                        }
                    } else {
                        // returnedCallback会在这之前处理，可能已经被修改为失败了
                        if (log.isWarnEnabled()) {
                            log.warn("rabbitmq发送了一条失败的消息，处理结果决定于returnedCallback" + correlationData.getId());
                        }
                    }
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库出现异常，等待分布式定时任务", e);
                    }
                }

            } else {
                // nack都不管，有分布式定时任务
                if (log.isErrorEnabled()) {
                    log.error("rabbitmq nack " + correlationData.getId() + "\t" + cause);
                }
            }
        }
    };


    /**
     * 不要随便修改名字
     * @return
     * @throws Exception
     */
    @SuppressWarnings("all")
    @Bean("emailRabbitTemplate")
    public RabbitTemplate emailRabbitTemplate() throws Exception {
        RabbitTemplate rabbitTemplate = abstractVhrRabbitTemplate.getObject();
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 路由失败的消息会走入这里，这个回调会先于任何的ack/nack，confirm会收到ack而不是nack，所以需要在这里处理。
             * 这相当于发送消息的两条分支，一般修改confirmCallback即可，这里作为备用的保障。
             * 将消息修改为发送失败
             * @param message
             * @param replyCode
             * @param replyText
             * @param exchange
             * @param routingKey
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                // 修改为失败状态
                ErrorMailSendLogEntity errorMailSendLogUDPEntity = new ErrorMailSendLogEntity();
                errorMailSendLogUDPEntity.setStatus(MessageConstants.SENDING);
                errorMailSendLogUDPEntity.setRoutingKey(routingKey);
                errorMailSendLogUDPEntity.setExchange(exchange);
                // 修改为失败状态
                String correctionId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
                errorMailSendLogUDPEntity.setMsgId(correctionId);
                try {
                    errorMailSendLogService.idempotentInConsumers(errorMailSendLogUDPEntity, MessageConstants.FAIL_IN_SEND);
                    if (log.isInfoEnabled()) {
                        log.info("成功将无法路由的消息持久化进数据库" + correctionId);
                    }
                    // 不可能出现不等于1的情况
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("无法路由的消息不能持久化进入数据库，出现异常：", e);
                    }
                }
            }
        });
        rabbitTemplate.setConfirmCallback(confirmCallback);
        return rabbitTemplate;
    }

}
