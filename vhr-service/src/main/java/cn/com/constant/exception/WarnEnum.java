package cn.com.constant.exception;

public enum WarnEnum implements ExceptionEnum {
    DEFAULT(2001,"输入参数错误"),
    DATE(2002,"非法日期参数"),
    ILLEGAL_OPERATION(2003,"用户非法操作")
    ;


    private int code;
    private String tip;

    WarnEnum(int code, String tip) {
        this.code = code;
        this.tip = tip;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getTip() {
        return tip;
    }
}
