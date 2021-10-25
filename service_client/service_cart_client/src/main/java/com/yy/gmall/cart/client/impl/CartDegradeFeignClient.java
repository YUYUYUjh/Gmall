package com.yy.gmall.cart.client.impl;

import com.yy.gmall.cart.client.CartFeignClient;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Yu
 * @create 2021-10-18 18:54
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {


    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum) {
        return null;
    }

    @Override
    public List<CartInfo> cartList() {
        return null;
    }

    @Override
    public Result deleteCart(Long skuId) {
        return null;
    }

    @Override
    public Result checkCart(Long skuId, Integer isChecked) {
        return null;
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        return null;
    }
}
