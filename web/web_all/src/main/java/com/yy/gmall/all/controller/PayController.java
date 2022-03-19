package com.yy.gmall.all.controller;

import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

/**
 * @author Yu
 * @create 2021-10-27 9:16
 */
@Controller
public class PayController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 去支付页面并展示信息
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/pay.html")
    public String toPay(Long orderId , Model model){
        //页面需要的 订单id ,  订单金额
        OrderInfo orderInfo = orderFeignClient.getOrderInfoAndOrderDetail(orderId);
        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    /**
     * 跳转到支付成功页面
     * 请求由AlipayApiController发出,重定向
     * http://payment.gmall.com/pay/success.html
     */
    @GetMapping("/pay/success.html")
    public String toPaySuccess(){
        return "payment/success";
    }

}
