package cn.com.mq.listener;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.ErrorMailSendLogEntity;
import cn.com.service.ErrorMailSendLogService;
import cn.com.service.SendErrorMailService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-14 18:10
 */
@Slf4j
@Component
public class ErrorEmailMQListener {

    @Autowired
    private SendErrorMailService sendErrorMailService;
    @Autowired
    private ErrorMailSendLogService errorMailSendLogService;
    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 缓存消息的重试次数
     */
    public static final String ERROR_EMAIL_CONSUME_COUNT_PREFIX = "mq:errorEMail:";
    public static final int ERROR_EMAIL_MESSAGE_EXPIRE_TIME = 2;
    public static final TimeUnit ERROR_EMAIL_MESSAGE_EXPIRE_TIME_UNIT = TimeUnit.HOURS;

    @RabbitListener(queues = {EMailConstants.ERROR_EMAIL_QUEUE})
    public void sendEmail(String errorMessageBody, Message message, Channel channel) {
        try {
            channel.basicQos(1);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("basicQos异常，可能是rabbitmq出现问题", e);
            }
            return;
        }
        ErrorMailSendLogEntity errorMailSendLogUDPEntity = new ErrorMailSendLogEntity();

        String correlationId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
        errorMailSendLogUDPEntity.setMsgId(correlationId);
        errorMailSendLogUDPEntity.setStatus(MessageConstants.SENT_SUCCESSFUL);
        // 300秒(可以用分布式定时任务，但是不太灵活，消费端可以有更高的灵活性)
        LocalDateTime now = LocalDateTime.now();
        //        errorMailSendLogUDPEntity.setTryTime(LocalDateTime.now().plusSeconds(EMailConstants.EMAIL_DELAY_TIME));
        errorMailSendLogUDPEntity.setUpdateTime(now);
        try {
            // 保障消息只会接收一次, 消息状态从 (1 发送成功) 到 (3 client已经接收)，重复的消息无法再根据 1 的状态进行修改
            sendErrorMailService.sendErrorMail(errorMailSendLogUDPEntity, MessageConstants.THE_CLIENT_RECEIVED_IT);
            // 正常结束，无论是重复发送的消息还是真的成功，都确认消息(上面的方法已经是幂等的了，走入这里就算成功)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 异常结束，需要重试
            if (log.isInfoEnabled()) {
                log.info("消息抵达消费端失败，重试中", e);
            }
            // redis 累计次数
            try {
                String retryCountStr = redisTemplate.opsForValue().get(ERROR_EMAIL_CONSUME_COUNT_PREFIX + correlationId);
                if (retryCountStr == null) {
                    redisTemplate.opsForValue().set(ERROR_EMAIL_CONSUME_COUNT_PREFIX + correlationId, "1", ERROR_EMAIL_MESSAGE_EXPIRE_TIME, ERROR_EMAIL_MESSAGE_EXPIRE_TIME_UNIT);
                    if (log.isInfoEnabled()) {
                        log.info("消息重试中，第 1 次 ：" + correlationId);
                    }
                } else {
                    int retryCount = Integer.parseInt(retryCountStr);
                    if (retryCount >= EMailConstants.CONSUMER_RETRY_COUNT) {
                        if (log.isWarnEnabled()) {
                            log.warn("重试次数已经达到最大次数，消息即将进入数据库或死信队列" + errorMailSendLogUDPEntity);
                        }
                        // 进入数据库或死信队列
                        this.entryDatabaseOrDeadLetterQueue(message, channel, errorMailSendLogUDPEntity);
                        // 走入这里，消息失败了，需要人工介入
                        return;
                    } else {
                        redisTemplate.opsForValue().increment(ERROR_EMAIL_CONSUME_COUNT_PREFIX + correlationId);
                    }
                    if (log.isInfoEnabled()) {
                        log.info("消息重试中，第 " + (retryCount + 1) +  " 次 ：" + correlationId);
                    }
                }
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("消息重试失败，可能是redis缓存出现问题，消息即将持久化进入数据库或死信队列" + errorMailSendLogUDPEntity, ex);
                }
                // 进入数据库或死信队列
                try {
                    this.entryDatabaseOrDeadLetterQueue(message, channel, errorMailSendLogUDPEntity);
                } catch (IOException exc) {
                    if (log.isErrorEnabled()) {
                        // 走到这里说明rabbitmq出现了问题，但是rabbitmq我们开启了持久化
                        log.error("rabbitmq出现异常，始终无法确认消息", exc);
                    }
                }
            }
            // redis 一切正常，进行重试
            // 这是重要的数据库状态，重回队列，redis累计次数
            try {
                // 重回队列会比较好，因为其它消费者可能就成功了，不采用定时任务(定时任务在不执行任务时依然会占据资源)
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                if (log.isErrorEnabled()) {
                    log.error("rabbitmq出现异常，始终无法确认消息", ex);
                }
            }
        }
    }

    private void entryDatabaseOrDeadLetterQueue(Message message, Channel channel, ErrorMailSendLogEntity errorMailSendLogUDPEntity) throws IOException {
        try {
            // 修改数据库状态
            errorMailSendLogService.idempotentInConsumers(errorMailSendLogUDPEntity, MessageConstants.FAIL_IN_CONSUME);
            if (log.isWarnEnabled()) {
                log.warn("消息已经达到最大重试次数，修改数据库状态成功(MessageConstants.FAIL_IN_CONSUME)成功" + errorMailSendLogUDPEntity);
            }
            // 确认消息，修改数据库状态成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            // 进入死信队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            if (log.isErrorEnabled()) {
                log.error("错误邮件数据库出现错误，进入死信队列成功" + errorMailSendLogUDPEntity, ex);
            }
        }
    }
}
