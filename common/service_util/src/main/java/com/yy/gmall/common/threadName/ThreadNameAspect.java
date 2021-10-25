package com.yy.gmall.common.threadName;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 切面实现线程名字注解
 * @author Yu
 * @create 2021-10-18 19:07
 */
@Component
@Aspect
public class ThreadNameAspect {

    @SneakyThrows
    @Around(value = "@annotation(com.yy.gmall.common.threadName.ThreadName)")
    public void cacheData(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        ThreadName threadName = methodSignature.getMethod().getAnnotation(ThreadName.class);
        String methodName = threadName.methodName();
        System.out.println(methodName+"----子线程开启" + Thread.currentThread().getName());
        pjp.proceed();
        System.out.println(methodName+"----子线程结束" + Thread.currentThread().getName());
    }
}
