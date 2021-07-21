package cn.com.config;

import cn.com.properties.TimedTaskProperties;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 定时任务是一种持续消耗资源的操作，要尽量少用，消费侧就可以不使用它
 * @author wyl
 * @create 2020-08-16 19:07
 */
@Configuration
@EnableConfigurationProperties(TimedTaskProperties.class)
public class TimedJobConfig {
    @Autowired
    private TimedTaskProperties timedTaskProperties;

    @Bean
    public CoordinatorRegistryCenter coordinatorRegistryCenter() {
        // 配置注册中心
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(timedTaskProperties.getServerLists(),
                timedTaskProperties.getNamespace());
        // 设置超时参数，重试次数需要设置大一点
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(timedTaskProperties.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(timedTaskProperties.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setMaxRetries(timedTaskProperties.getMaxRetries());
        // 初始化注册中心
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        regCenter.init();
        return regCenter;
    }
}
