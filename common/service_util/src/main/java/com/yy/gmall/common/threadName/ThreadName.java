package com.yy.gmall.common.threadName;

import java.lang.annotation.*;

/**
 * 自定义注解:将方法前后加上线程名
 * @author Yu
 * @create 2021-10-18 19:06
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ThreadName {

    String methodName() default "";
}
