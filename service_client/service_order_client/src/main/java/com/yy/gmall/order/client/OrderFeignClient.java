package com.yy.gmall.order.client;

import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-19 20:59
 */
@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    @GetMapping("/api/order/auth/trade")
    public Map trade();

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("/api/order/auth/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);

    /**
     * 查询订单信息(包含订单明细)
     * @param orderId
     * @return
     */
    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfoAndOrderDetail(@PathVariable Long orderId);
}
