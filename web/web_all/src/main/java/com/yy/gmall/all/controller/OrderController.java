package com.yy.gmall.all.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author Yue
 * @create 2021-10-19 21:01
 */
@Controller
public class OrderController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("trade.html")
    public String toTrade(Model model){
        Map map = orderFeignClient.trade();
        model.addAllAttributes(map);
        return "order/trade";
    }

    @GetMapping("myOrder.html")
    public String toMyOrder(){

        return "order/myOrder";
    }
}
