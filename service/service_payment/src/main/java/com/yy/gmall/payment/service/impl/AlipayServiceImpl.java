package com.yy.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.yy.gmall.model.enums.OrderStatus;
import com.yy.gmall.model.enums.PaymentStatus;
import com.yy.gmall.model.enums.PaymentType;
import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.model.payment.PaymentInfo;
import com.yy.gmall.order.client.OrderFeignClient;
import com.yy.gmall.payment.config.AlipayConfig;
import com.yy.gmall.payment.service.AlipayService;
import com.yy.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yu
 * @create 2021-10-27 15:48
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    //远程调用获取订单信息
    @Autowired
    private OrderFeignClient orderFeignClient;

    //远程调用支付宝接口
    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private PaymentService paymentService;


    @Override
    public String submit(Long orderId) {
        //        //1.实例化客户端
        //        //公共参数
        //        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do",  //1.支付宝的网关地址
        //                "2021000118638674",     //2.APPID
        //                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDARZ+mqvX0Kr3xq+ijZFMqmPyf0kdot3g8wt1NQiJ7joTQJ0L0KSIE5pOroKSPC2mMvNqq4dWVw6mhVRx3C0SNFUUBAFMYDUm1uQ/NhGT3ROEEEIIqxKy1qiF7wPWn+Lvd6QSM4IX+2yqJvt0YnZNiF9OPVpHsKpVxcKc8aCrPkqL5zcRFvEeBhsoLRs1NrA4+wECdQq3n4KHhDemyS1TPezIGeuoRujYPJU9JH7FaRysZRkjAm207x7pBpNuctg01uXqex0xpWX+Wsk7UkLLrheB/Cx7vYpP6USGd9Mv+LFe2aPPj3n1pH8BUDtYbCvvDyq9X0mzw3KEkxBdyOANDAgMBAAECggEATKg1k8ybjQKxgjYI5YPtSJGAPvQmdlCXxpnmg5P3jY1v1FGi9xejMZMAky/UDBf/uBXflLu/XRuKeQEm/Stn5FvzjAemGXPKzIIBE+5SFufGUsmGLCkHttIi0WVIS/T2ae4qW0R1B/w3hH2tRtAkX7qiJYbdXOxJctX4RzQ8xU6/O/y9WzzBCdkSqQv6fv/G7ziC8KO/6M39m+BRIsasSmdmKpV3GzbqM1Rqqo68d6yrtHmV0UxxrqNdVidvbEMQ7pv29eqEsbfEtzh5WQO5gNM86waJJ2NZPZPZ6nXR5Mvbi9QcnLNuQrdA7i9iO/lbnQB7ulPj6gEKNYhUB0Te0QKBgQDpQUW5NdtXhRZfGRp+qSp2HsWvRhyOO5YJfUzyLAUWO8jmJ80SLpKlRtotaMrVG4aNMql9MUyOW9gUtrgYzxJQ6C4zPlWYubl386jUaLkLh0y/TeDu2KLjwNXw8mYEcBWlA2ScQFnXXTznD9SJcXl87ctReZUg9VHVWMn3y583SQKBgQDTBUug0VR/K9ktiypNnTRSWMpVNh/bkxzYs7jYOG9OJ37VhoY+oyWhobvudYPFThgQ4iRXXzMYQhZi6g+sekb9eDQdQh22HQ8jt3uOI5Cx6yDJB/s09YA0NTzsAYOgXl4FUzXLx9jW2zn7UYebln8cCLLox4/Wt4IsElZ1wWnqKwKBgHROLZvqNNJw8/S7KbDaXOfE9EEvLnHlWCynI8TlGiw55zSLx/825bwvlf5qcCjOrsqc5lIcUNXzCc1aRnVoWvOosdCvVjLu+IHNJvOyvAH5ltUU2gF+V8pEjuizlRbFZKWBCZu3opR2UzXLeOiHglI+p9CaVTe32vQMb7h0+ZCxAoGBAIOBh1IfYjMmll/FnIVl9NdksiNMaaojEhUSW52T/fmMr4ROI5chgTArZL0EyyvIsULES7WPhH4XoW/fQglQ4rDPmlqpe26kyWyOyhcYnvHiADqIGMs2O8FNdDv37ZmhdaKH6rymPMIZORcgBgy2S1yjJyveQgujrSHeA7e641ClAoGBANwEHGkdoO8wUPjpwb2L/CPUS+NsY+535XE1k+joNGXCpf8ghVrkwHgRiXX1AgviDRLw+cPz6PMN9eFPsCLhFAp9QmQkapt362LnKkqtu0QLYrqIEph39Ez7mHbdzVPxp4JT6uO7WfZ88ICv1Lfp3T+rOb502fjbVSWracyU1b5s",    //3. 商家私钥 我们的私钥负责签名
        //                "json", //4.发送的数据格式
        //                "UTF-8", //5.数据的编码
        //                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8EkZ/2bVoAyXKPfhbbpmeBMSdDx51+UpXOnKdEvHNMQMpLmVCekMUD8I4ctKjgtPzkGO9rm0ITfQeQkVUuWlTet1FGRXGaaWaqYmMZH2QvN6fyE126IF280HYGyDDGgGTGIoZJp4mG8gW0Tf4T+fM97PirLsvxpvwXX1it/CYSQUav9B62jPQMyHCaQ7QSbQ28nDDwDFlqxzKtvzO45AFvjePz6Cy7DXMaBgzHcfJO856CBjWw5M/d3CUeK/iAfHSaWV1dOp69L3C5K8w71mzwAtPM6Q8J41tESIfq/sZKUY7+JJdICS1q4HwyBOkoe54Zqpwris3AD+m9LajLyUzwIDAQAB",  //6.支付宝的公钥 负责加密
        //                "RSA2"  // 7.加密方式
        //        );
        //2. 8.接口名称, 统一下单接单并支付接口   alipay.trade.page.pay
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        //异步回调
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        //同步回调
        request.setReturnUrl(AlipayConfig.return_payment_url);

        //获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfoAndOrderDetail(orderId);
        if (OrderStatus.CLOSED.name().equals(orderInfo.getOrderStatus()) || OrderStatus.PAID.name().equals(orderInfo.getOrderStatus())){
            return "订单已关闭或者订单已支付";
        }
        //保存交易记录
        PaymentInfo paymentInfo = paymentService.insertPaymentInfo(orderInfo, PaymentType.ALIPAY.name());
        //请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo()); //支付宝的交易编号 , 要求唯一
        bizContent.put("total_amount", orderInfo.getTotalAmount());//总金额  上限一个小目标
        bizContent.put("subject", orderInfo.getTradeBody());  //
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");//销售产品码，与支付宝签约的产品码名称。注：目前电脑支付场景下仅支持FAST_INSTANT_TRADE_PAY
        //bizContent.put("qr_pay_mode","2");
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(calendar.HOUR,2);
//        bizContent.put("time_expire", calendar.getTime()); //订单绝对超时时间。注：time_expire和timeout_express两者只需传入一个或者都不传，两者均传入时，优先使用time_expire

        request.setBizContent(bizContent.toString());
        //3.发出请求
        AlipayTradePagePayResponse response = null;
        try {

            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println("响应体:" + response.getBody());
        } else {
            System.out.println("调用失败");
        }
        return response.getBody();
    }

    /**
     * 退款
     * @param outTradeNo
     */
    @Override
    public void refund(String outTradeNo) {
        //退款接口 alipay.trade.refund
        AlipayTradeRefundRequest alipayTradeRefundRequest = new AlipayTradeRefundRequest();

        //准备参数
        //查询支付表
        PaymentInfo paymentInfo = paymentService.selectPayment(outTradeNo);
        JSONObject jsonObject = new JSONObject();
        //订单编号
        jsonObject.put("out_trade_no",paymentInfo.getOutTradeNo());
        //退款金额
        jsonObject.put("refund_amount",paymentInfo.getTotalAmount());

        alipayTradeRefundRequest.setBizContent(jsonObject.toJSONString());
        try {
            alipayClient.pageExecute(alipayTradeRefundRequest);
            //paymentInfo.setPaymentStatus("已退款");
            System.out.println("退款成功");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
    }
}
