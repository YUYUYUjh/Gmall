package com.yy.gmall.list.client.impl;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.list.client.ListFeignClient;
import com.yy.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

/**
 * @author Yu
 * @create 2021-10-13 16:07
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result onSale(Long skuId) {
        return null;
    }

    @Override
    public Result cancelSale(Long skuId) {
        return null;
    }

    @Override
    public void incrHotScore(Long skuId, Integer score) {

    }

    @Override
    public Result searchData(SearchParam searchParam) {
        return null;
    }
}
