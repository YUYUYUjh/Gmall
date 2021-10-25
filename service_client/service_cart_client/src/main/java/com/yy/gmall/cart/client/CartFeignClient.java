package com.yy.gmall.cart.client;

import com.yy.gmall.cart.client.impl.CartDegradeFeignClient;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.util.AuthContextHolder;
import com.yy.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Yu
 * @create 2021-10-18 18:54
 */
@FeignClient(value = "service-cart", fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    /**
     * 添加购物车 支持临时用户添加购物车
     * 在发布数据接口的时候,如果有request对象,则不需要处理:它的作用是获取用户的id,而用户id在请求头中
     * 微服务传递数据的时候,是没有携带请求头的,需要
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable Long skuId,
                              @PathVariable Integer skuNum);


    /**
     * 查看购物车列表
     * @param request
     * @return
     */
    @GetMapping("/api/cart/cartList")
    public List<CartInfo> cartList();

    /**
     * 根据用户id删除购物项
     */
    @DeleteMapping("/api/cart/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId);

    /**
     * 更新选中状态
     */
    @GetMapping("/api/cart/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked);

    /**
     * 获取选中状态的购物车列表
     */
    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable Long userId);

}
