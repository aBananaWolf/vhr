package cn.com.mq.listener;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.EmployeeEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.service.SendWelcomeMailService;
import cn.com.service.WelcomeMailLogService;
import cn.com.vo.EmployeeEmailVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-19 14:17
 */
@Slf4j
@Component
public class WelcomeEmailMQListener {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SendWelcomeMailService sendWelcomeMailService;
    @Autowired
    private WelcomeMailLogService welcomeMailLogService;

    public static final String WELCOME_EMAIL_SEND_RETRY_COUNT_PREFIX = "mq:empEmail:";
    public static final int WELCOME_EMAIL_SEND_RETRY_COUNT_TIME = 2;
    public static final TimeUnit WELCOME_EMAIL_SEND_RETRY_COUNT_TIME_UNIT = TimeUnit.HOURS;

    @RabbitListener(queues = EMailConstants.EMP_EMAIL_QUEUE)
    public void welcomeEmailMQListener(@Payload EmployeeEmailVO emp, Message message, Channel channel) {
        try {
            channel.basicQos(1);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("basicQos异常，可能是rabbitmq出现异常", e);
            }
        }
        WelcomeMailLogEntity welcomeMailLogEntity = new WelcomeMailLogEntity();
        welcomeMailLogEntity.setStatus(MessageConstants.SENT_SUCCESSFUL);
        welcomeMailLogEntity.setUpdateTime(LocalDateTime.now());
        String correlationId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
        welcomeMailLogEntity.setMsgId(correlationId);

        try {
//            int a = 1 / 0;
            // 保障消息只会消费一次，消息状态从 (1 发送成功) 到 (3 客户端已接收)，重复消息无法再根据 1 的状态进行修改
            sendWelcomeMailService.sendWelcomeMail(welcomeMailLogEntity, emp);
            //  正常结束，无论是重复发送的消息还是真的成功，都确认消息(上面的方法已经是幂等的了，走入这里就算成功)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 异常结束，需要重试
            if (log.isWarnEnabled()) {
                log.warn("消息抵达消费端失败，开始重试", e);
            }
            // redis 累计次数
            try {
                String retryCountStr = redisTemplate.opsForValue().get(WELCOME_EMAIL_SEND_RETRY_COUNT_PREFIX + correlationId);
                if (retryCountStr == null) {
                    redisTemplate.opsForValue().set(WELCOME_EMAIL_SEND_RETRY_COUNT_PREFIX + correlationId, "1", WELCOME_EMAIL_SEND_RETRY_COUNT_TIME, WELCOME_EMAIL_SEND_RETRY_COUNT_TIME_UNIT);
                    if (log.isInfoEnabled()) {
                        log.info("发送入职邮件失败，开始重试，第 1 次");
                    }
                } else {
                    int retryCount = Integer.parseInt(retryCountStr);
                    if (retryCount >= EMailConstants.EMAIL_SENT_RETRY_COUNT) {
                        if (log.isErrorEnabled()) {
                            log.error("消息重试次数已达到最大重试次数，消息即将持久化进数据库或进入死信队列");
                        }
                        this.entryDatabaseOrDeadLetterQueue(welcomeMailLogEntity, message, channel);
                        // 走入这里，消息失败了，需要人工介入
                        return;
                    } else {
                        redisTemplate.opsForValue().increment(WELCOME_EMAIL_SEND_RETRY_COUNT_PREFIX + correlationId);
                    }
                    if (log.isInfoEnabled()) {
                        log.info("发送入职邮件失败，开始重试，第 " + (retryCount + 1) + " 次");
                    }
                }
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("消息无法重试，可能是redis缓存出现异常，消息即将持久化进数据库或进入死信队列", ex);
                }
                try {
                    this.entryDatabaseOrDeadLetterQueue(welcomeMailLogEntity, message, channel);
                } catch (Exception exc) {
                    if (log.isErrorEnabled()) {
                        log.error("始终无法确认消息，rabbitmq 出现异常", ex);
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
                    log.error("始终无法确认消息，rabbitmq 出现异常", ex);
                }
            }
        }
    }

    private void entryDatabaseOrDeadLetterQueue(WelcomeMailLogEntity welcomeMailLogEntity, Message message, Channel channel) throws IOException {
        try {
//            int a = 1 / 0;
            welcomeMailLogService.idempotentInConsumers(welcomeMailLogEntity, MessageConstants.FAIL_IN_CONSUME);
            if (log.isInfoEnabled()) {
                log.info("成功修改数据库状态FAIL_IN_CONSUME, 消息成功持久化进入数据库成功" + welcomeMailLogEntity);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);

            if (log.isErrorEnabled()) {
                log.error("消息无法持久化进入数据库，但进入死信队列成功", e);
            }
        }
    }
}
