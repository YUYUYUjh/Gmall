package com.yy.gmall.list.service;

import com.yy.gmall.model.list.SearchParam;
import com.yy.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * @author Yu
 * @create 2021-10-13 11:16
 */
public interface ListService {

    /**
     * 上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     *  热点
     * @param skuId
     * @param score
     */
    void incrHotScore(Long skuId, Integer score);

    /**
     * 商品搜索
     * @param searchParam
     * @return
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
