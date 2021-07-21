package cn.com.mq.sender;

import cn.com.constant.message.EMailConstants;
import cn.com.constant.message.MessageConstants;
import cn.com.entities.EmployeeEntity;
import cn.com.entities.WelcomeMailLogEntity;
import cn.com.service.WelcomeMailLogService;
import cn.com.util.IdGenerator;
import cn.com.vo.EmployeeEmailVO;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * @author wyl
 * @create 2020-08-18 15:22
 */
@Slf4j
@Component
public class WelcomeEMailSender {
    @Autowired
    @Qualifier("welcomeMailRabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WelcomeMailLogService welcomeMailLogService;
    @Autowired
    private ObjectMapper objectMapper;

    public void sendWelcomeEMail(String correlationId, String empJson) {
        rabbitTemplate.send(
                EMailConstants.EMP_EMAIL_EXCHANGE,
                EMailConstants.EMP_EMAIL_ROUTING_KEY,
                MessageBuilder.withBody(String.valueOf(empJson).getBytes())
                   .andProperties(
                           MessagePropertiesBuilder.newInstance()
                                .setContentEncoding("UTF-8")
                                .setContentType("application/json")
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                                .build()
                   ).build(),
                new CorrelationData(correlationId)
                );
    }

    // 抛出异常
    public void sendWelcomeEMailAndSaveLog(WelcomeMailLogEntity welcomeMailLogEntity, EmployeeEmailVO employeeEmailVO) {
        // 时间
        LocalDateTime now = LocalDateTime.now();
        welcomeMailLogEntity.setUpdateTime(now);
        welcomeMailLogEntity.setTryTime(now.plusSeconds(EMailConstants.EMAIL_DELAY_TIME));
        // 发送中
        welcomeMailLogEntity.setStatus(MessageConstants.SENDING);
        // 交换机等属性
        welcomeMailLogEntity.setExchange(EMailConstants.EMP_EMAIL_EXCHANGE);
        welcomeMailLogEntity.setRoutingKey(EMailConstants.EMP_EMAIL_ROUTING_KEY);
        welcomeMailLogEntity.setAttemptCount(0);
        // 唯一id
        String correlationId = IdGenerator.getId();
        welcomeMailLogEntity.setMsgId(correlationId);
        welcomeMailLogService.save(welcomeMailLogEntity);
        try {
            this.sendWelcomeEMail(correlationId, objectMapper.writeValueAsString(employeeEmailVO));
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("errorMail发送失败，可能是rabbitmq出现异常", e);
            }
            // throw e; 不需要这个操作，因为有分布式定时任务了，捕获了就捕获了
        }
    }

    // 定时任务专用
    @Transactional(timeout = -1)
    public void sendWelcomeEMailAndUpdateLog(WelcomeMailLogEntity welcomeMailLogEntity, EmployeeEmailVO employeeEmailVO) throws Exception {
        welcomeMailLogEntity.setStatus(MessageConstants.SENDING);
        welcomeMailLogEntity.setUpdateTime(LocalDateTime.now());
        int affected = welcomeMailLogService.idempotentInConsumers(welcomeMailLogEntity, MessageConstants.SENT_SUCCESSFUL);
        if (affected != 1) {
            return;
        }
        this.sendWelcomeEMail(welcomeMailLogEntity.getMsgId(),objectMapper.writeValueAsString(employeeEmailVO));
    }
}
