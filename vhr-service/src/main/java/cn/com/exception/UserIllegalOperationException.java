package cn.com.exception;

import cn.com.constant.exception.ExceptionEnum;

/**
 * @author wyl
 * @create 2020-08-04 12:23
 */
public class UserIllegalOperationException extends RuntimeException implements GlobalException {
    private ExceptionEnum exceptionEnum;
    private Object obj;

    public UserIllegalOperationException(ExceptionEnum message) {
        super(message.getTip());
        this.exceptionEnum = message;
    }
    public UserIllegalOperationException(ExceptionEnum message, Object... obj) {
        super(message.getTip());
        this.exceptionEnum = message;
        this.obj = obj;
    }

    public UserIllegalOperationException(ExceptionEnum message, Throwable throwable) {
        super(message.getTip(),throwable);
        this.exceptionEnum = message;
    }

    public UserIllegalOperationException(String message) {
        super(message);
        exceptionEnum = new ExceptionEnum() {
            @Override
            public int getCode() {
                return 7777;
            }

            @Override
            public String getTip() {
                return message;
            }
        };
    }

    public UserIllegalOperationException(String message, Throwable cause) {
        super(message, cause);
        exceptionEnum = new ExceptionEnum() {
            @Override
            public int getCode() {
                return 7777;
            }

            @Override
            public String getTip() {
                return message;
            }
        };
    }

    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public Object getObj() {
        return obj;
    }
}
