package cn.com.mq.listener;

import cn.com.constant.message.MessageConstants;
import cn.com.constant.message.SmsCodeConstants;
import cn.com.entities.SmsVerificationCodeSendLogEntity;
import cn.com.service.SendSmsCodeService;
import cn.com.service.SmsVerificationCodeSendLogService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jni.Local;
import org.apache.tomcat.jni.Time;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-17 20:36
 */
@Slf4j
@Component
public class SmsCodeMQListener {
    @Autowired
    private SmsVerificationCodeSendLogService smsVerificationCodeSendLogService;
    @Autowired
    private SendSmsCodeService sendSmsCodeService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 缓存消息的重试次数
     */
    public static final String SMS_CODE_MESSAGE_CONSUME_COUNT_PREFIX = "mq:smsCode:";
    public static final int SMS_CODE_CONSUME_COUNT_EXPIRE_TIME = 2;
    public static final TimeUnit SMS_CODE_CONSUME_COUNT_EXPIRE_TIME_UNIT = TimeUnit.HOURS;

    @RabbitListener(queues = SmsCodeConstants.SMS_CODE_QUEUE)
    public void smsCodeMQListener(String body, Message message, Channel channel) {
        try {
            channel.basicQos(1);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("basicQos异常，可能是rabbitmq出现异常", e);
            }
            return;
        }
        String correlationId = message.getMessageProperties().getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
        String phoneAndCodeStr = body.trim();
        String[] phoneAndCode = phoneAndCodeStr.split("\\:");
        SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity = new SmsVerificationCodeSendLogEntity();
        // 小的校验，一般来说发来的消息不会有错
        if (ArrayUtils.isEmpty(phoneAndCode) || phoneAndCode.length != 2) {
            if (log.isErrorEnabled()) {
                log.error("smsCode消息格式错误");
            }
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("rabbitmq出现异常，始终无法确认消息", e);
                }
            }
        }

        smsVerificationCodeSendLogEntity.setMsgId(correlationId);
        smsVerificationCodeSendLogEntity.setUpdateTime(LocalDateTime.now());
        smsVerificationCodeSendLogEntity.setPhone(Long.parseLong(phoneAndCode[0].trim()));
        smsVerificationCodeSendLogEntity.setCode(phoneAndCode[1].trim());
        smsVerificationCodeSendLogEntity.setStatus(MessageConstants.SENT_SUCCESSFUL);

        try {
            // 异常测试
//            int a = 1 / 0;
            // 保障消息只会接收一次, 消息状态从 (1 发送成功) 到 (3 client已经接收)，重复的消息无法再根据 1 的状态进行修改
            sendSmsCodeService.sendSmsCode(smsVerificationCodeSendLogEntity, MessageConstants.THE_CLIENT_RECEIVED_IT);
            //  正常结束，无论是重复发送的消息还是真的成功，都确认消息(上面的方法已经是幂等的了，走入这里就算成功)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 异常结束，需要重试
            if (log.isInfoEnabled()) {
                log.info("消息抵达消费端失败，开始重试");
            }
            // redis 累计次数
            try {
                String retryCountStr = redisTemplate.opsForValue().get(SMS_CODE_MESSAGE_CONSUME_COUNT_PREFIX + correlationId);
                if (retryCountStr == null) {
                    redisTemplate.opsForValue().set(SMS_CODE_MESSAGE_CONSUME_COUNT_PREFIX + correlationId, "1", SMS_CODE_CONSUME_COUNT_EXPIRE_TIME, SMS_CODE_CONSUME_COUNT_EXPIRE_TIME_UNIT);
                    if (log.isInfoEnabled()) {
                        log.info("消息重试中，第 1 次 ：" + correlationId);
                    }
                } else {
                    int retryCount = Integer.parseInt(retryCountStr);
                    if (retryCount >= SmsCodeConstants.SMS_CODE_CONSUME_RETRY_COUNT) {
                        if (log.isWarnEnabled()) {
                            log.warn("重试次数已达到最大重试次数，即将持久化进入数据库或死信队列：" + smsVerificationCodeSendLogEntity );
                        }
                        // 持久化进数据库或死信队列
                        this.entryDatabaseOrDeadQueue(message,channel,smsVerificationCodeSendLogEntity);
                        // 走入这里，消息失败了，需要人工介入
                        return;
                    } else {
                        redisTemplate.opsForValue().increment(SMS_CODE_MESSAGE_CONSUME_COUNT_PREFIX + correlationId);
                    }
                    if (log.isInfoEnabled()) {
                        log.info("消息重试中，第 " + (retryCount + 1) +  " 次 ：" + correlationId);
                    }
                }
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("消息重试失败，可能是redis缓存出现问题，消息即将持久化进入数据库或死信队列", ex);
                }
                // 进入数据库或死信队列
                try {
                    this.entryDatabaseOrDeadQueue(message,channel,smsVerificationCodeSendLogEntity);
                } catch (Exception exc) {
                    // 走到这里说明rabbitmq出现了问题，但是rabbitmq我们开启了持久化
                    if (log.isErrorEnabled()) {
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

    private void entryDatabaseOrDeadQueue(Message message, Channel channel, SmsVerificationCodeSendLogEntity smsVerificationCodeSendLogEntity) throws IOException {
        try {
            // 异常测试
//            int a = 1 / 0;

            smsVerificationCodeSendLogService.idempotentInConsumers(smsVerificationCodeSendLogEntity, MessageConstants.FAIL_IN_CONSUME);

            if (log.isWarnEnabled()) {
                log.warn("消息已达到最大重试次数，修改数据库状态FAIL_IN_CONSUME成功,correlationId：" + smsVerificationCodeSendLogEntity.getMsgId() );
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);

            if (log.isErrorEnabled()) {
                log.error("修改数据库状态失败，但成功进入死信队列,correlationId：" + smsVerificationCodeSendLogEntity.getMsgId() , e);
            }

        }
    }
}
