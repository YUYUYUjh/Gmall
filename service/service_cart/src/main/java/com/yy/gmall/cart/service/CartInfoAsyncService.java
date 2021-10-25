package com.yy.gmall.cart.service;

import com.yy.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-18 16:11
 */
public interface CartInfoAsyncService {

    /**
     * 异步修改DB购物车
     * @param cartInfoExist
     */
    void updateAsyncCart(CartInfo cartInfoExist);

    /**
     * 异步添加DB购物车
     * @param cartInfoExist
     */
    void insertAsyncCart(CartInfo cartInfoExist);

    /**
     * 异步合并
     * @param cartInfoList
     */
    void merge(String userId, String userTempId, List<CartInfo> cartInfoList);


    /**
     * 异步删除购物项
     * @param userIdOrTempId
     * @param skuId
     */
    void deleteByUserIdAndSkuId(String userIdOrTempId, Long skuId);
}
