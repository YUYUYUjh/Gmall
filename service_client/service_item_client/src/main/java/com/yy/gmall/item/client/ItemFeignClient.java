package com.yy.gmall.item.client;

import com.yy.gmall.item.client.impl.ItemDegradeFeignClient;
import com.yy.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-03 21:53
 */
@FeignClient(value = "service-item", fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {

    @GetMapping("/api/item/{skuId}")
    public Map getItemBySkuId(@PathVariable Long skuId);
}



