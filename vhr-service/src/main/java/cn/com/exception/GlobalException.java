package cn.com.exception;

import cn.com.constant.exception.ExceptionEnum;

public interface GlobalException {
    public ExceptionEnum getExceptionEnum() ;

    public Object getObj();
}
