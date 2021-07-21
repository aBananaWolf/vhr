package cn.com.util;

import cn.com.anno.FuzzyFlag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author wyl
 * @create 2020-08-08 13:02
 */
@Slf4j
public class VhrFuzzyQueryUtils {
    private static final Object[] nonArgs = new Object[]{null};

    public static void fuzzyProcessing(Object obj) {
        try {
            Class<?> aClass = obj.getClass();
            Field[] declaredFields = aClass.getDeclaredFields();

            for (Field declaredField : declaredFields) {
                FuzzyFlag annotation = declaredField.getAnnotation(FuzzyFlag.class);
                if (annotation == null) {
                    continue;
                }
                String fieldStr = declaredField.getName();

                Method originGetMethod = aClass.getDeclaredMethod("get" + fieldStr.substring(0, 1).toUpperCase() + fieldStr.substring(1));
                String param = (String) originGetMethod.invoke(obj, new Object[]{});
                Method originSetMethod = aClass.getDeclaredMethod("set" + fieldStr.substring(0, 1).toUpperCase() + fieldStr.substring(1), String.class);
                if (StringUtils.isBlank(param)) {
                    originSetMethod.invoke(obj, nonArgs);
                    continue;
                } else {
                    originSetMethod.invoke(obj, "%" + param + "%");
                }
            }
        } catch (ReflectiveOperationException e) {
            log.error("模糊查询处理失败", e);
        }
    }
}
