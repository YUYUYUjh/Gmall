package com.yy.gmall.payment.service;

/**
 * @author Yu
 * @create 2021-10-27 15:47
 */
public interface AlipayService {

    /**
     * 统一收单
     * @param orderId
     * @return
     */
    String submit(Long orderId);

    /**
     * 退款
     * @param outTradeNo
     */
    void refund(String outTradeNo);
}
