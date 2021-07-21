package cn.com.security.verification.code.sms.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author wyl
 * @create 2020-08-02 18:25
 */
public class SmsCodeCheckException extends AuthenticationException {

    public SmsCodeCheckException(String msg) {
        super(msg);
    }

    public SmsCodeCheckException(String msg, Throwable t) {
        super(msg, t);
    }
}
