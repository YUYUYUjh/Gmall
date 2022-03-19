package com.yy.gmall.payment.service;

import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-29 10:15
 */
public interface PaymentService {
    /**
     * 保存交易记录
     * @param orderInfo    订单数据
     * @param paymentType  支付类型
     * @return
     */
    PaymentInfo insertPaymentInfo(OrderInfo orderInfo,String paymentType);

    /**
     * 根据订单编号查询订单信息
     * @param outTradeNo
     * @return
     */
    PaymentInfo selectPayment(String outTradeNo);

    /**
     * 更新支付表
     * @param paramsMap
     */
    void updatePaymentInfo(Map<String, String> paramsMap,PaymentInfo paymentInfo);
}
