package com.yy.gmall.cart.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.yy.gmall.cart.service.CartService;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.util.AuthContextHolder;
import com.yy.gmall.model.cart.CartInfo;
import com.yy.gmall.model.cart.CartInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-17 21:29
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车 支持临时用户添加购物车
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){
        String userId = getUserIdOrTempId(request);
        CartInfo cartInfo = cartService.addToCart(skuId, skuNum, userId);

        return Result.ok(cartInfo);
    }



    /**
     * 查看购物车列表
     * @param request
     * @return
     */
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.getCartList(userId, userTempId);

        return Result.ok(cartInfoList);
    }
    /**
     * 根据用户id删除购物项
     */
    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request){
        String userIdOrTempId = this.getUserIdOrTempId(request);
        cartService.deleteCart(skuId,userIdOrTempId);
        return Result.ok();
    }
    /**
     * 更新选中状态
     */
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){
        String userIdOrTempId = this.getUserIdOrTempId(request);
        cartService.checkCart(skuId,isChecked,userIdOrTempId);
        return  Result.ok();
    }

    /**
     * 获取选中状态的购物车列表
     */
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable Long userId){
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
        return cartInfoList;
    }

    /**
     * 从请求头获取用户id或临时用户id
     * @param request
     * @return
     */
    private String getUserIdOrTempId(HttpServletRequest request) {
        //获取添加购物车的用户的id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //如果用户id为空,获取临时用户id
            userId = AuthContextHolder.getUserTempId(request);
        }
        return userId;
    }


}
