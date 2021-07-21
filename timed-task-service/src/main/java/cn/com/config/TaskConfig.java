package cn.com.config;

import cn.com.task.ErrorEmailRetryTimedTask;
import cn.com.task.SmsVerificationCodeRetryTimedTask;
import cn.com.task.WelcomeMailRetryTimedTask;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

/**
 * 仅是定义了简单类型的分布式定时任务
 * @author wyl
 * @create 2020-08-17 10:59
 */
@Configuration
public class TaskConfig {
    @Autowired
    private CoordinatorRegistryCenter coordinatorRegistryCenter;
    @Autowired
    private ErrorEmailRetryTimedTask errorEmailRetryTimedTask;
    @Autowired
    private SmsVerificationCodeRetryTimedTask smsVerificationCodeRetryTimedTask;
    @Autowired
    private WelcomeMailRetryTimedTask welcomeMailRetryTimedTask;

    @PostConstruct
    void init() {
        this.errorEMailSchedule();
        this.smsVerificationCodeSchedule();
        this.welComeEMailSchedule();
    }

    private void errorEMailSchedule() {
        JobConfiguration errorEmailConfig = JobConfiguration.newBuilder("cn.com.job.task.ErrorEmailRetryTimedTask", 1)
                .shardingItemParameters("0=Shenzheng")
                // 5 分钟
                .cron("0 0/5 * * * ?")
                .build();
        new ScheduleJobBootstrap(coordinatorRegistryCenter,errorEmailRetryTimedTask, errorEmailConfig).schedule();
    }

    private void smsVerificationCodeSchedule() {
        JobConfiguration smsVerifyCodeConfig = JobConfiguration.newBuilder("cn.com.job.task.SmsVerificationCodeRetryTimedTask", 1)
                .shardingItemParameters("0=Shenzheng")
                .cron("0/5 * * * * ?")
                .build();
        new ScheduleJobBootstrap(coordinatorRegistryCenter,smsVerificationCodeRetryTimedTask, smsVerifyCodeConfig).schedule();
    }

    private void welComeEMailSchedule() {
        JobConfiguration welcomeMailConfig = JobConfiguration.newBuilder("cn.com.job.task.WelcomeMailRetryTimedTask", 1)
                .shardingItemParameters("0=Shenzheng")
                .cron("0/5 * * * * ?")
                .build();
        new ScheduleJobBootstrap(coordinatorRegistryCenter, welcomeMailRetryTimedTask, welcomeMailConfig).schedule();
    }
}
