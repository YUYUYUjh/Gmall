package com.yy.gmall.list.client;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.list.client.impl.ListDegradeFeignClient;
import com.yy.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yu
 * @create 2021-10-13 16:07
 */

@FeignClient(value = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    /**
     * 上架
     * @return
     */
    @GetMapping("/api/list/inner/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId);

    /**
     * 下架
     * @return
     */
    @GetMapping("/api/list/inner/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId);

    /**
     * 根据skuId增加热点值
     * @param skuId
     * @param score
     */
    @PutMapping("/api/list/inner/incrHotScore/{skuId}/{score}")
    public void incrHotScore(@PathVariable Long skuId,@PathVariable Integer score);

    /**
     * 商品检索
     */
    @PostMapping("/api/list/inner/searchData")
    public Result searchData(@RequestBody SearchParam searchParam);
}
