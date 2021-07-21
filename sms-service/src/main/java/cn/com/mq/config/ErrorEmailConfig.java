package cn.com.mq.config;

import cn.com.constant.message.EMailConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.Topic;

import java.util.HashMap;

/**
 * @author wyl
 * @create 2020-08-14 18:36
 */
@Configuration
public class ErrorEmailConfig {
    @Bean
    public TopicExchange errorEmailExchange() {
        return ExchangeBuilder.topicExchange(EMailConstants.ERROR_EMAIL_EXCHANGE).durable(true).build();
    }

    // 死信队列
    @Bean
    public Queue errorEmailQueue() {
        return QueueBuilder.durable(EMailConstants.ERROR_EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMailConstants.ERROR_EMAIL_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMailConstants.REMIND_EMAIL_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding errorEmailBinding(TopicExchange errorEmailExchange, Queue errorEmailQueue) {
        return BindingBuilder.bind(errorEmailQueue)
                .to(errorEmailExchange)
                .with(EMailConstants.REMIND_EMAIL_ROUTING_KEY);
    }

    @Bean
    public TopicExchange errorEmailDeadExchange() {
        return ExchangeBuilder.topicExchange(EMailConstants.ERROR_EMAIL_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue errorEmailDeadQueue() {
        return QueueBuilder.durable(EMailConstants.ERROR_EMAIL_DEAD_QUEUE).build();
    }

    @Bean
    public Binding errorEmailDeadBinding(TopicExchange errorEmailDeadExchange, Queue errorEmailDeadQueue) {
        return BindingBuilder.bind(errorEmailDeadQueue)
                .to(errorEmailDeadExchange)
                .with(EMailConstants.REMIND_EMAIL_DEAD_ROUTING_KEY);
    }
}
