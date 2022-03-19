package com.yy.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.service.RabbitService;
import com.yy.gmall.model.enums.PaymentStatus;
import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.model.payment.PaymentInfo;
import com.yy.gmall.payment.mapper.PaymentInfoMapper;
import com.yy.gmall.payment.service.PaymentService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-29 10:15
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RabbitService rabbitService;


    @Override
    public PaymentInfo insertPaymentInfo(OrderInfo orderInfo, String paymentType) {
        //先查询是否已经生成了支付表,(防止重复保存订单)
        PaymentInfo paymentInfo = paymentInfoMapper
                .selectOne(new QueryWrapper<PaymentInfo>().eq("order_id",orderInfo.getId()));
        if (paymentInfo!=null){
            //已经生成了订单表 直接修改支付类型就ok
            paymentInfo.setPaymentType(paymentType);
            paymentInfoMapper.updateById(paymentInfo);
            return paymentInfo;
        }
        //数据库没有支付表
        //创建一个PaymentInfo对象
        paymentInfo = new PaymentInfo();

        //赋值
//        @ApiModelProperty(value = "对外业务编号")
//        @TableField("out_trade_no")
//        private String outTradeNo;
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
//        @ApiModelProperty(value = "订单编号")
//        @TableField("order_id")
//        private Long orderId;
        paymentInfo.setOrderId(orderInfo.getId());
//        @ApiModelProperty(value = "支付类型（微信 支付宝）")
//        @TableField("payment_type")
//        private String paymentType;
        paymentInfo.setPaymentType(paymentType);
//        @ApiModelProperty(value = "交易编号")
//        @TableField("trade_no")
//        private String tradeNo;
        //paymentInfo.setTradeNo();
//        @ApiModelProperty(value = "支付金额")
//        @TableField("total_amount")
//        private BigDecimal totalAmount;
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
//        @ApiModelProperty(value = "交易内容")
//        @TableField("subject")
//        private String subject;
        paymentInfo.setSubject(orderInfo.getTradeBody());
//        @ApiModelProperty(value = "支付状态")
//        @TableField("payment_status")
//        private String paymentStatus;
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
//        @ApiModelProperty(value = "创建时间")
//        @TableField("create_time")
//        private Date createTime;
        paymentInfo.setCreateTime(new Date());
//        @ApiModelProperty(value = "回调时间")
//        @TableField("callback_time")
//        private Date callbackTime;

//        @ApiModelProperty(value = "回调信息")
//        @TableField("callback_content")
//        private String callbackContent;

        //直接插入数据
        paymentInfoMapper.insert(paymentInfo);
        return paymentInfo;
    }

    /**
     * 查询paymentInfo
     * @param outTradeNo
     * @return
     */
    @Override
    public PaymentInfo selectPayment(String outTradeNo) {

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no", outTradeNo));

        return paymentInfo;
    }

    /**
     * 更新支付表
     * @param paramsMap
     */
    @Override
    public void updatePaymentInfo(Map<String, String> paramsMap,PaymentInfo paymentInfo) {
        //1.注意幂等性问题  判断支付状态
        if (PaymentStatus.UNPAID.name().equals(paymentInfo.getPaymentStatus())){
            //2.更新支付表
            //2.1 支付状态改为已支付
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            //2.2 银行流水
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            //2.3 回调时间
            try {
                SimpleDateFormat sdf = new SimpleDateFormat();
                Date notifyTime = sdf.parse(paramsMap.get("notify_time"));
                paymentInfo.setCallbackTime(notifyTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //2.4 回调内容
            paymentInfo.setCallbackContent(JSONObject.toJSONString(paramsMap));

            paymentInfoMapper.updateById(paymentInfo);

            //3.发消息MQ 通知订单 更新订单状态
            rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    MQConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
        }
    }
}
