package cn.com.constant.message;

import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-14 11:04
 */
public interface EMailConstants {
    /**
     * 数据库重试一次，间隔
     */
    int DAO_DELAY_TIME = 2;
    /**
     * 发送的前300秒都是正常范围
     */
    int EMAIL_DELAY_TIME = 300;
    /**
     * 单体服务数据库重试时间单位(仅用于错误邮件，因为无法将错误响应给用户，让用户重试。那么只能努力让邮件成功)
     */
    TimeUnit EMAIL_DELAY_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * 消息发送重试次数
     */
    int EMAIL_SENT_RETRY_COUNT = 3;

    /**
     * 消费者消费重试次数
     */
    int CONSUMER_RETRY_COUNT = 3;
    String ERROR_EMAIL_EXCHANGE = "vhr_email_exchange";
    String ERROR_EMAIL_QUEUE = "vhr_email_queue";
    String ERROR_EMAIL_DEAD_EXCHANGE = "vhr_email_dead_exchange";
    String ERROR_EMAIL_DEAD_QUEUE = "vhr_email_dead_queue";
    /**
     * 项目异常提醒邮件
     */
    String REMIND_EMAIL_ROUTING_KEY = "vhr.email.remind";
    String REMIND_EMAIL_DEAD_ROUTING_KEY = "vhr.email.dead.remind";


    /**
     * emp欢迎邮件
     */
    String EMP_EMAIL_EXCHANGE = "vhr_emp_email_exchange";
    String EMP_EMAIL_QUEUE = "vhr_emp_email_queue";
    String EMP_EMAIL_DEAD_EXCHANGE = "vhr_emp_email_dead_exchange";
    String EMP_EMAIL_DEAD_QUEUE = "vhr_emp_email_dead_queue";
    String EMP_EMAIL_ROUTING_KEY = "vhr.email.emp";
    String EMP_EMAIL_DEAD_ROUTING_KEY = "vhr.email.dead.emp";
}
