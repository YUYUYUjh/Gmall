package com.yy.gmall.product.service.impl;

import com.yy.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Yu
 * @create 2021-10-10 9:15
 */
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private RedisTemplate redisTemplate;

    private final Lock lock = new ReentrantLock();

    @Override
    public void testLock() {
        lock.lock();
        try {
            Integer num = (Integer) redisTemplate.opsForValue().get("num");
            num++;
            redisTemplate.opsForValue().set("num",num);
        } finally {

            lock.unlock();
        }
    }
}
