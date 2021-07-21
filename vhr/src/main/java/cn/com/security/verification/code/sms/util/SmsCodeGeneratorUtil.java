package cn.com.security.verification.code.sms.util;

import cn.com.security.verification.code.sms.raw.SmsCode;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-04 14:33
 */
@Component
public class SmsCodeGeneratorUtil {

    public SmsCode createCode(int expireTime, TimeUnit timeUnit) {
        return new SmsCode(expireTime,timeUnit,this.doCreateCode());
    }

    public String doCreateCode() {
        return String.valueOf( (int) ((1 + Math.random()) * 100000) );
    }
}
