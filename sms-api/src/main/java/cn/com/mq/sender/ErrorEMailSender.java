package cn.com.mq.sender;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.mq.threadpool.MessageScheduleThreadPool;
import cn.com.service.ErrorMailSendLogService;
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
 * @create 2020-08-15 10:15
 */
@Slf4j
@Component
public class ErrorEMailSender {
    @Autowired
    @Qualifier("emailRabbitTemplate")
    private RabbitTemplate emailRabbitTemplate;
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;
    @Autowired
    private MessageScheduleThreadPool messageScheduleThreadPool;

    public void sendErrorMailLog(String message, String correlationId) {
        emailRabbitTemplate.send(
                EMailConstants.ERROR_EMAIL_EXCHANGE,
                EMailConstants.REMIND_EMAIL_ROUTING_KEY,
                MessageBuilder.withBody(message.getBytes())
                        .andProperties(
                                MessagePropertiesBuilder.newInstance()
                                        .setContentEncoding("UTF-8")
                                        // 字符串，rabbitmq的contentType是有意义的
                                        .setContentType("text/plain")
                                        // 持久化消息
                                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                                        .build()
                        ).build(),
                new CorrelationData(correlationId));
    }

    public void sendErrorEMailAndSaveLog(ErrorMailSendLogEntity errorMailSendLogEntity) {
        // 交换机等属性
        errorMailSendLogEntity.setExchange(EMailConstants.ERROR_EMAIL_EXCHANGE);
        errorMailSendLogEntity.setRoutingKey(EMailConstants.REMIND_EMAIL_ROUTING_KEY);
        errorMailSendLogEntity.setStatus(MessageConstants.SENDING);
        errorMailSendLogEntity.setCount(0);
        LocalDateTime now = LocalDateTime.now();
        errorMailSendLogEntity.setCreateTime(now);
        errorMailSendLogEntity.setUpdateTime(now);
        // 定时任务五分钟后捕获
        errorMailSendLogEntity.setTryTime(now.plusSeconds(EMailConstants.EMAIL_DELAY_TIME));
        // 消息唯一id
        String correlationId = IdGenerator.getId();
        errorMailSendLogEntity.setMsgId(correlationId);
        // 不需要消息体,充数用的
        final String message = "vhr";
        try {
            // 错误邮件，努力尝试成功，不回滚
            errorMailSendLogService.save(errorMailSendLogEntity);
        } catch (Exception ex) {
            if (log.isWarnEnabled()) {
                log.warn("vhr(globalExceptionHandler)持久层异常 - warn : ", ex);
            }
            // 延迟任务，2秒后重试一次
            messageScheduleThreadPool.schedule(() -> {
                try {
                    errorMailSendLogService.save(errorMailSendLogEntity);
                } catch (Exception exc) {
                    if (log.isErrorEnabled()) {
                        log.error("vhr(globalExceptionHandler)持久层异常 - error : " + errorMailSendLogEntity, exc);
                        log.error("错误的具体消息" + errorMailSendLogEntity);
                    }
                    return;
                }
                // 重试后成功即可，失败则以上记录错误日志
                try {
                    this.sendErrorMailLog(message, correlationId);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("errorMail发送失败，可能是rabbitmq出现异常", e);
                    }
                }
            }, EMailConstants.DAO_DELAY_TIME, EMailConstants.EMAIL_DELAY_TIME_UNIT);
            // 不再走后面的逻辑
            return;
        }
        // 正常逻辑
        try {
            this.sendErrorMailLog(message, correlationId);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("errorMail发送失败，可能是rabbitmq出现异常", e);
            }
        }
    }

    // 定时任务专用
    @Transactional(timeout = -1)
    public void sendErrorEMailAndUpdateLog(ErrorMailSendLogEntity mailSendLogEntity) {
        mailSendLogEntity.setStatus(MessageConstants.SENDING);
        mailSendLogEntity.setUpdateTime(LocalDateTime.now());
        int affected = errorMailSendLogService.idempotentInConsumers(mailSendLogEntity, MessageConstants.SENT_SUCCESSFUL);
        if (affected != 1) {
            return;
        }
        this.sendErrorMailLog(mailSendLogEntity.getBody(), mailSendLogEntity.getMsgId());
    }
}
