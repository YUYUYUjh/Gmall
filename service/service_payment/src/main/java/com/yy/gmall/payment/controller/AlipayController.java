package com.yy.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.model.enums.PaymentStatus;
import com.yy.gmall.model.payment.PaymentInfo;
import com.yy.gmall.payment.config.AlipayConfig;
import com.yy.gmall.payment.service.AlipayService;
import com.yy.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

/**
 * @author Yu
 * @create 2021-10-27 15:44
 */
//@RestController
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 统一收单 ,下单并支付接口
     * @param orderId
     * @return  把支付宝返回的字符串返回到页面
     */
    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable Long orderId){

        //返回一个JSON字符串,(实则一大堆页面样式)
        String res = alipayService.submit(orderId);
        //通过@ResponseBody直接把返回的字符串扔到页面上,
        return res;
    }

    /**
     * 设置同步回调地址:  经过网关 (网关是对外(互联网暴露在外))
     *  http://api.gmall.com/api/payment/alipay/callback/return
     */
    @GetMapping("/callback/return")
    public String callBack(){
        //重定向到支付成功页面
        //http://payment.gmall.com/pay/success.html
        //注意:回调路径必须与AlipayConfig中传给DefaultAlipayClient的参数一致
        return "redirect:" + AlipayConfig.return_order_url;
    }

    /**
     * 设置异步回调  经过网关  注意是post请求
     * /api/payment/alipay/callback/notify
     */
    @PostMapping("/callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramsMap, HttpServletRequest request){
        System.out.println(paramsMap.toString());
     /*
        @RequestParam注解底层干的事:
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        Map<String,String> paramsMap = new HashMap<>();
        paramsMap.put("id",id);
        paramsMap.put("name",name);
    */
     /*
     Map数据:
     {
        gmt_create=2021-10-29 20:32:30,
        charset=utf-8, gmt_payment=2021-10-29 20:32:56,
        notify_time=2021-10-29 20:32:57,
        subject=Apple iPhone 13 Pro Max (A2644) 5G全网通双卡双待全面屏长续航手机 银色 1T【官方标配】,
        sign=AOg2QzGJblVR0qF56I0FL5tHHSLdnXbkCNW1PJKS2yYAvwZ5+6CfTD+v+FmUFDiwVV8WH6///+u9TRrbZVzchETAZpvB30KSFoBtSSUawyIbCdyrk9GGalOeEhFwUVIi7H07fzafVGhQ6+2UXYI7eYfjqRHJBL9BNwHCNcZk9guz3osUXDt1schglnDdV8g8UPbxuVbwicl0c9rgGHjOhcmTZEfIKeheKZeEq6rOIw3QrAKUnuPfRl+z1WyO3MoOutCmgo7zxFF4Q+Ekvg78YiiD9aAmmJgf2m9lCupxC1HU2l/EKxQFrEQ4r1RWmFzYVbwcsHck5t7W9/j0TklpFQ==,
        buyer_id=2088622957111473,
        invoice_amount=25998.00,
        version=1.0,
        notify_id=2021102900222203256011470515133489,
        fund_bill_list=[{"amount":"25998.00","fundChannel":"ALIPAYACCOUNT"}],
        notify_type=trade_status_sync, out_trade_no=1635471513302423,
        total_amount=25998.00,
        trade_status=TRADE_SUCCESS,
        trade_no=2021102922001411470501744914,
        auth_app_id=2021000118638674,
        receipt_amount=25998.00,
        point_amount=0.00,
        app_id=2021000118638674,
        buyer_pay_amount=25998.00,
        sign_type=RSA2,
        seller_id=2088621956758805
      }
      */
        //1.验签
        try {
            boolean res = AlipaySignature.rsaCheckV1(paramsMap,
                    AlipayConfig.alipay_public_key,
                    AlipayConfig.charset,
                    AlipayConfig.sign_type);
            if (res) {

                //签名校验成功
                //订单后续处理!!!

                //1.校验交易状态 ,校验状态需要是支付成功
                String trade_status = paramsMap.get("trade_status");
                if ("TRADE_SUCCESS".equals(trade_status)){
                    //2.校验用户支付的金额是否正确
                    //获取支付宝返回的支付金额
                    String totalAmount = paramsMap.get("total_amount");
                    System.out.println("支付宝返回的总金额:"+totalAmount);
                    PaymentInfo paymentInfo = paymentService.selectPayment(paramsMap.get("out_trade_no"));
                    //判断支付金额是否和数据库相等,判断支付状态是否是支付中(幂等性问题)
                    if (paymentInfo.getTotalAmount().toString().equals(totalAmount)&&paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID.name())){
                        //金额正确,支付状态是支付中
                        //更新支付表
                        paymentService.updatePaymentInfo(paramsMap,paymentInfo);
                    }else{
                        //金额不正确
                        System.out.println("金额不正确");
                        return "failure";
                    }

                }

                System.out.println("交易成功");
                return "success";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println("交易不成功");
        return "failure";
    }

    /**
     * 统一退款接口
     */
    @ResponseBody
    @GetMapping("/refund/{outTradeNo}")
    public Result refund(@PathVariable String outTradeNo){
        alipayService.refund(outTradeNo);
        return Result.ok();
    }
}
