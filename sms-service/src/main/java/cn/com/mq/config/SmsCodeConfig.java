package cn.com.mq.config;

import cn.com.constant.message.SmsCodeConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wyl
 * @create 2020-08-17 18:57
 */
@Configuration
public class SmsCodeConfig {
    @Bean
    public TopicExchange smsCodeExchange() {
        return ExchangeBuilder.topicExchange(SmsCodeConstants.SMS_CODE_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue smsCodeQueue() {
        return QueueBuilder.durable(SmsCodeConstants.SMS_CODE_QUEUE)
                .withArgument("x-dead-letter-exchange", SmsCodeConstants.SMS_CODE_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SmsCodeConstants.SMS_CODE_DEAD_ROUTING_KEY)
                .build()
                ;
    }
    @Bean
    public Binding smsCodeBinding(TopicExchange smsCodeExchange, Queue smsCodeQueue) {
        return BindingBuilder.bind(smsCodeQueue)
                .to(smsCodeExchange)
                .with(SmsCodeConstants.SMS_CODE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange smsCodeDeadExchange() {
        return ExchangeBuilder.topicExchange(SmsCodeConstants.SMS_CODE_DEAD_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue smsCodeDeadQueue() {
        return QueueBuilder.durable(SmsCodeConstants.SMS_CODE_DEAD_QUEUE).build();
    }
    @Bean
    public Binding smsCodeDeadBinding(TopicExchange smsCodeDeadExchange, Queue smsCodeDeadQueue) {
        return BindingBuilder.bind(smsCodeDeadQueue)
                .to(smsCodeDeadExchange)
                .with(SmsCodeConstants.SMS_CODE_DEAD_ROUTING_KEY);
    }
}
