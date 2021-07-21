package cn.com.constant;

public enum TaskShardingItemEnum{
    /**
     * 分片数据
     */
    Shenzheng(0, "深圳"),

    ;

    private int item;
    private String address;

    TaskShardingItemEnum(int item, String address) {
        this.item = item;
        this.address = address;
    }


    public static TaskShardingItemEnum get(int i) {
        switch (i) {
            default:
                return Shenzheng;
        }
    }

    public int getItem() {
        return item;
    }

    public String getAddress() {
        return address;
    }


}
