package cn.com.mq.config;

import cn.com.constant.message.EMailConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wyl
 * @create 2020-08-19 18:24
 */
@Configuration
public class EmpEmailConfig {
    @Bean
    public TopicExchange EMPEmailExchange() {
        return ExchangeBuilder.topicExchange(EMailConstants.EMP_EMAIL_EXCHANGE).durable(true).build();
    }

    // 死信队列
    @Bean
    public Queue EMPEmailQueue() {
        return QueueBuilder.durable(EMailConstants.EMP_EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMailConstants.EMP_EMAIL_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMailConstants.EMP_EMAIL_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding EMPEmailBinding(TopicExchange EMPEmailExchange, Queue EMPEmailQueue) {
        return BindingBuilder.bind(EMPEmailQueue)
                .to(EMPEmailExchange)
                .with(EMailConstants.EMP_EMAIL_ROUTING_KEY);
    }

    @Bean
    public TopicExchange EMPEmailDeadExchange() {
        return ExchangeBuilder.topicExchange(EMailConstants.EMP_EMAIL_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue EMPEmailDeadQueue() {
        return QueueBuilder.durable(EMailConstants.EMP_EMAIL_DEAD_QUEUE).build();
    }

    @Bean
    public Binding EMPEmailDeadBinding(TopicExchange EMPEmailDeadExchange, Queue EMPEmailDeadQueue) {
        return BindingBuilder.bind(EMPEmailDeadQueue)
                .to(EMPEmailDeadExchange)
                .with(EMailConstants.EMP_EMAIL_DEAD_ROUTING_KEY);
    }
}
