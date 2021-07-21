package cn.com.constant.message;

public interface SmsCodeConstants {
    String SMS_CODE_EXCHANGE = "vhr_sms_code_exchange";
    String SMS_CODE_QUEUE = "vhr_sms_code_queue";
    String SMS_CODE_ROUTING_KEY = "vhr.sms.code";

    /**
     * 短信验证码发送的前五秒都很正常
     */
    int SMS_CODE_SENT_DELAY_TIME = 5;

    int SMS_CODE_SENT_RETRY_COUNT = 2;
    int SMS_CODE_CONSUME_RETRY_COUNT = 3;

    String SMS_CODE_DEAD_EXCHANGE = "vhr_sms_code_dead_exchange";
    String SMS_CODE_DEAD_QUEUE = "vhr_sms_code_dead_queue";
    String SMS_CODE_DEAD_ROUTING_KEY = "vhr.sms.dead_code";

}
