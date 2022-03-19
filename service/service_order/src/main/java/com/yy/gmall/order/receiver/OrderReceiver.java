package com.yy.gmall.order.receiver;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.entity.GmallCorrelationData;
import com.yy.gmall.common.service.RabbitService;
import com.yy.gmall.model.enums.OrderStatus;
import com.yy.gmall.model.enums.ProcessStatus;
import com.yy.gmall.order.service.OrderInfoService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-26 21:16
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private RabbitService rabbitService;

    //监听取消订单的消息
    @RabbitListener(queues = MQConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Message message, Channel channel){
        try {
            //调用取消订单方法
            byte[] body = message.getBody();
            String orderId = new String(body);
            orderInfoService.cancelOrder(Long.parseLong(orderId));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("接收的时间:"+sdf.format(new Date())+"----------"+"接收到的消息:"+new String(message.getBody()));
            //手动确认  参数一:消息标识符  参数二:是否批量应答 (预期值有关 0:轮询分发 1-10:公平分发)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听下单支付成功后 更新订单状态
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            value = @Queue(value = MQConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            key = MQConst.ROUTING_PAYMENT_PAY
    ))
    public void updateOrderStatusById(Long orderId , Channel channel,Message message){
        System.out.println("接收到的订单Id:"+orderId);
        try {

            orderInfoService.updateOrderStatusById(orderId, ProcessStatus.PAID);

            //给库存系统发消息,
            Map map = orderInfoService.initWareOrder(orderId);
            rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_WARE_STOCK,
                    MQConst.ROUTING_WARE_STOCK, JSONObject.toJSON(map));
            //手动确认  参数一:消息标识符  参数二:是否批量应答 (预期值有关 0:轮询分发 1-10:公平分发)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听仓库扣减库存是否成功
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_WARE_ORDER),
            value = @Queue(value = MQConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            key = MQConst.ROUTING_WARE_ORDER
    ))
    public void wareIsSuc(String result , Channel channel,Message message){
        System.out.println("接收到的减库存结果:"+result);
        try {

            Map resMap = JSONObject.parseObject(result, Map.class);
            if ("DEDUCTED".equals(resMap.get("status"))){
                //扣减成功  修改订单的状态
                long orderId = Long.parseLong(String.valueOf(resMap.get("orderId")));
                orderInfoService.updateOrderStatusById(orderId, ProcessStatus.WAITING_DELEVER);
            }else{
                //库存超卖
                long orderId = Long.parseLong(String.valueOf(resMap.get("orderId")));
                orderInfoService.updateOrderStatusById(orderId, ProcessStatus.STOCK_EXCEPTION);
                //打日志转人工
            }

            //手动确认  参数一:消息标识符  参数二:是否批量应答 (预期值有关 0:轮询分发 1-10:公平分发)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
