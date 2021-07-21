package cn.com.constant.exception;

public enum SucceedEnum implements ExceptionEnum {
    INSERT(3001,"新增成功"),
    UPDATE(3002,"更新成功"),
    DELETE(3003,"删除成功"),
    SELECT(3004,"查询成功"),
    UPLOAD(3007,"上传成功"),
    EXCEL_IMPORT(3010,"excel导入成功"),
    EXCEL_EXPORT(3010,"excel导出成功"),
    ;

    private int code;
    private String tip;

    SucceedEnum(int code, String tip) {
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
