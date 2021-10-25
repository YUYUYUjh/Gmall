package com.yy.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.yy.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu
 * @create 2021-10-08 20:16
 *
 * 分布式锁+ aop切面实现类
 * 1.创建自定义注解
 * 2.编写切面类 实现类自定义注解的逻辑缓存
 *
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient; // 分布式锁
    @Autowired
    private RedisTemplate redisTemplate;

    //定义一个环绕通知 实现类逻辑缓存
    //找注解 切入点表达式 而不是找类 找方法
    @SneakyThrows //自动抛异常
    @Around(value = "@annotation(com.yy.gmall.common.cache.GmallCache)")
    public Object cacheData(ProceedingJoinPoint pjp) {
        //定义一个对象
        Object obj = new Object();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        //获取到注解上的前缀
        String prefix = gmallCache.prefix();
        //组成缓存的key 获取方法传递参数
        String key = prefix+Arrays.asList(pjp.getArgs()).toString()+ RedisConst.SKUKEY_SUFFIX;
        //可以通过这个key获取缓存的数据
        try {
            obj = this.getRedisData(key,methodSignature);
            if (obj == null) {
                //分布式锁
                //设置分布式锁, 进入数据库进行查询数据
                RLock lock = redissonClient.getLock(key + ":lock");
                //调用tryLock方法
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断
                if (res) {
                    try {
                        //执行业务逻辑,直接从数据库获取数据
                        obj = pjp.proceed(pjp.getArgs());
                        //防止穿透
                        if (obj==null){
                            obj = methodSignature.getReturnType().newInstance();
                            this.redisTemplate.opsForValue().set(key, obj,5,TimeUnit.MINUTES);
                            return obj;
                        }
                        this.redisTemplate.opsForValue().set(key, obj,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return obj;
                    } finally {
                        //解锁
                        lock.unlock();
                    }
                }else {
                    //没有获取到锁
                    try {
                        Thread.sleep(100);
                        return cacheData(pjp);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
            return obj;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //数据库兜底
        return pjp.proceed(pjp.getArgs());
    }

    /**
     * 从缓存获取数据
     * @param key
     * @return
     */
    private Object getRedisData(String key,MethodSignature methodSignature) {
        //在向缓存存储数据的时候,将数据变为json字符串了
        //
        Object o = this.redisTemplate.opsForValue().get(key);
        if (o!=null){
            return o;
        }
        return null;
    }
/*        //方法的标签
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        //方法上的注解
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //缓存的key
        String cacheKey = gmallCache.prefix() +
                Arrays.asList(pjp.getArgs()) + RedisConst.SKUKEY_SUFFIX;
        //分布式锁的key
        String cacheLock = gmallCache.prefix() +
                Arrays.asList(pjp.getArgs()) + RedisConst.SKULOCK_SUFFIX;
        //方法的返回值
        Class returnType = signature.getReturnType();
        //获取缓存中的数据
        Object o = redisTemplate.opsForValue().get(cacheKey);
        if (null != o) {
            return o;
        }
        RLock lock = redissonClient.getLock(cacheLock);
        try {
            boolean res = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (res){
                //上锁成功 去方法中查询结果
                Object result = pjp.proceed(pjp.getArgs());
                //防止缓存穿透
                if (null == result) {
                    result = returnType.newInstance();
                    redisTemplate.opsForValue().set(cacheKey,
                            result,3,TimeUnit.SECONDS);
                }else {
                    redisTemplate.opsForValue().set(cacheKey,
                            result,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                }
                return result;
            }else{
                //上锁失败  睡一会 直接获取缓存中的数据
                Thread.sleep(2000);
                return redisTemplate.opsForValue().get(cacheKey);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }*/

}
