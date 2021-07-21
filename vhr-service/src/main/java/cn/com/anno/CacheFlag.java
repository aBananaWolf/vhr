package cn.com.anno;


import cn.com.constant.cache.CacheTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheFlag {

    String cachePrefix();

    String lockPrefix();

    int keyPosition() default -1;

    CacheTypeEnum cacheType() default CacheTypeEnum.WRITE;
}
