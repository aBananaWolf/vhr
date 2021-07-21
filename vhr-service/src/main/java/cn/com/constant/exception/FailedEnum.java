package cn.com.constant.exception;

public enum FailedEnum implements ExceptionEnum {
    INSERT(3001,"新增失败,当前在线人数较多"),
    UPDATE(3002,"更新失败"),
    DELETE(3003,"删除失败"),
    SELECT(3004,"查询失败"),
    SERVICE(3005,"服务器被吃了(⊙▽⊙)~"),
    DEFAULT(3006,"参数缺省"),
    UPLOAD(3007,"上传失败"),
    DEFAULT2(3008,"传入参数错误"),
    EXCEL_IMPORT(3010,"excel导入错误"),
    EXCEL_EXPORT(3010,"excel导出错误"),
    CORRELATION(3011,"该数据有关联数据，操作失败"),
    SMS_VERIFICATION_CODE_SENT(3012,"当前在线人数较多，请稍后再试"),
    VHR(8888,"vhr项目异常"),
    VHR_DETAIL_TIPS(8889,"vhr项目未捕获异常，仔细检查"),

    ;

    private int code;
    private String tip;

    FailedEnum(int code, String tip) {
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
