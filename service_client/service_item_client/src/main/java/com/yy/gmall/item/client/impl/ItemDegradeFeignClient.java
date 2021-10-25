package com.yy.gmall.item.client.impl;

import com.yy.gmall.item.client.ItemFeignClient;
import com.yy.gmall.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-03 21:54
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Map getItemBySkuId(Long skuId) {
        return null;
    }
}
