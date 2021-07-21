package cn.com.security.verification.code.img.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author wyl
 * @create 2020-08-02 18:25
 */
public class ImageCodeCheckException extends AuthenticationException {

    public ImageCodeCheckException(String msg) {
        super(msg);
    }

    public ImageCodeCheckException(String msg, Throwable t) {
        super(msg, t);
    }
}
