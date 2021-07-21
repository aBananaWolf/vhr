package cn.com.constant.cache;

public enum CacheTypeEnum {
    READ(1), WRITE(2);

    private int value;

    CacheTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
