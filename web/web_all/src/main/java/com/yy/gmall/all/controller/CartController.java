package com.yy.gmall.all.controller;

import com.yy.gmall.cart.client.CartFeignClient;
import com.yy.gmall.common.interceptor.FeignInterceptor;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.model.cart.CartInfo;
import com.yy.gmall.model.product.SkuInfo;
import com.yy.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Yu
 * @create 2021-10-18 19:35
 */
@Controller
public class CartController {

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 网关拦截addCart.html路径,
     * 在web-all微服务中匹配Mapping路径
     * @param request
     * @return
     */
    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request){

        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(Long.parseLong(skuId));
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "cart/addCart";
    }

    @GetMapping("cart.html")
    public String toCartHtml(){

        return "cart/index";
    }
}
