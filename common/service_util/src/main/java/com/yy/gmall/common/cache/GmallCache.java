package com.yy.gmall.common.cache;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author Yu
 * @create 2021-10-08 20:12
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {

    //定义一个属性 sku:skuId
    //目的 用这个前缀组成想要的缓存的key
    String prefix() default "cache";
}
