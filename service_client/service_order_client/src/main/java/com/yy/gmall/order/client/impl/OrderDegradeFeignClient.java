package com.yy.gmall.order.client.impl;

import com.yy.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-19 21:00
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public Map trade() {
        return null;
    }
}
