package cn.com.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wyl
 * @create 2020-08-17 13:00
 */
@ConfigurationProperties(prefix = "elasticjob.reg-center")
@Setter
@Getter
public class TimedTaskProperties {
    private String serverLists;
    private String namespace;
    private int baseSleepTimeMilliseconds;
    private int maxSleepTimeMilliseconds;
    private int maxRetries;
}
