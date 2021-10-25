package com.yy.gmall.cart.service;

import com.yy.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-17 21:40
 */
public interface CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param skuNum
     * @param userId
     */
    CartInfo addToCart(Long skuId, Integer skuNum, String userId);

    /**
     * 查看购物车列表
     * @param
     * @return
     */
    List<CartInfo> getCartList(String userId,String userTempId);

    /**
     * 删除购物项
     * @param skuId
     * @param userIdOrTempId
     */
    void deleteCart(Long skuId, String userIdOrTempId);

    /**
     * 更新选中状态
     * @param skuId
     * @param isChecked
     * @param userIdOrTempId
     */
    void checkCart(Long skuId, Integer isChecked, String userIdOrTempId);

    /**
     * 获取对应用户的购物选中列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(Long userId);
}
