package cn.com.util;

import java.util.UUID;

/**
 * @author wyl
 * @create 2020-08-18 15:18
 */
public class IdGenerator {
    public static String getId() {
        return UUID.randomUUID().toString();
    }
}
