package com.yy.gmall.activity.client.impl;

import com.yy.gmall.activity.client.ActivityFeignClient;
import com.yy.gmall.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * @author Yu
 * @create 2021-11-01 22:12
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {


    @Override
    public Result findAll() {
        return Result.fail();
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return Result.fail();
    }
}
