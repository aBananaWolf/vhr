package cn.com.mq.common;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 就是拷贝autoConfiguration，区分不同的rabbitTemplate
 * {@link RabbitAutoConfiguration.RabbitTemplateConfiguration#rabbitTemplate(RabbitProperties, ObjectProvider, ObjectProvider, ConnectionFactory)}
 * @author wyl
 * @create 2020-08-14 11:49
 */
@Configuration
public class AbstractVhrRabbitTemplate implements FactoryBean<RabbitTemplate> {
    @Autowired
    private RabbitProperties properties;
    @Autowired
    private ObjectProvider<MessageConverter> messageConverter;
    @Autowired
    private ObjectProvider<RabbitRetryTemplateCustomizer> retryTemplateCustomizers;
    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public RabbitTemplate getObject() throws Exception {
        return abstractVhrRabbitTemplate(properties,messageConverter,retryTemplateCustomizers,connectionFactory);
    }

    @Override
    public Class<?> getObjectType() {
        return RabbitTemplate.class;
    }

    public RabbitTemplate abstractVhrRabbitTemplate(RabbitProperties properties,
                                                    ObjectProvider<MessageConverter> messageConverter,
                                                    ObjectProvider<RabbitRetryTemplateCustomizer> retryTemplateCustomizers,
                                                    ConnectionFactory connectionFactory) {
        PropertyMapper map = PropertyMapper.get();
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        messageConverter.ifUnique(template::setMessageConverter);
        template.setMandatory(determineMandatoryFlag(properties));
        RabbitProperties.Template templateProperties = properties.getTemplate();
        if (templateProperties.getRetry().isEnabled()) {
            template.setRetryTemplate(
                    new RetryTemplateFactory(retryTemplateCustomizers.orderedStream().collect(Collectors.toList()))
                            .createRetryTemplate(templateProperties.getRetry(),
                                    RabbitRetryTemplateCustomizer.Target.SENDER));
        }
        map.from(templateProperties::getReceiveTimeout).whenNonNull().as(Duration::toMillis)
                .to(template::setReceiveTimeout);
        map.from(templateProperties::getReplyTimeout).whenNonNull().as(Duration::toMillis)
                .to(template::setReplyTimeout);
        map.from(templateProperties::getExchange).to(template::setExchange);
        map.from(templateProperties::getRoutingKey).to(template::setRoutingKey);
        map.from(templateProperties::getDefaultReceiveQueue).whenNonNull().to(template::setDefaultReceiveQueue);
        return template;
    }

    private boolean determineMandatoryFlag(RabbitProperties properties) {
        Boolean mandatory = properties.getTemplate().getMandatory();
        return (mandatory != null) ? mandatory : properties.isPublisherReturns();
    }

    class RetryTemplateFactory {

        private final List<RabbitRetryTemplateCustomizer> customizers;

        RetryTemplateFactory(List<RabbitRetryTemplateCustomizer> customizers) {
            this.customizers = customizers;
        }

        RetryTemplate createRetryTemplate(RabbitProperties.Retry properties, RabbitRetryTemplateCustomizer.Target target) {
            PropertyMapper map = PropertyMapper.get();
            RetryTemplate template = new RetryTemplate();
            SimpleRetryPolicy policy = new SimpleRetryPolicy();
            map.from(properties::getMaxAttempts).to(policy::setMaxAttempts);
            template.setRetryPolicy(policy);
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            map.from(properties::getInitialInterval).whenNonNull().as(Duration::toMillis)
                    .to(backOffPolicy::setInitialInterval);
            map.from(properties::getMultiplier).to(backOffPolicy::setMultiplier);
            map.from(properties::getMaxInterval).whenNonNull().as(Duration::toMillis).to(backOffPolicy::setMaxInterval);
            template.setBackOffPolicy(backOffPolicy);
            if (this.customizers != null) {
                for (RabbitRetryTemplateCustomizer customizer : this.customizers) {
                    customizer.customize(target, template);
                }
            }
            return template;
        }
    }
}
