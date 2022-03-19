package com.yy.gmall.list.receiver;

import com.rabbitmq.client.Channel;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.list.service.ListService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Yu
 * @create 2021-10-25 17:50
 *
 * 监听消息
 *
 */
@Component
public class ListReceiver {

    @Autowired
    private ListService listService;

    //实现商品上架
    //监听消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_GOODS_UPPER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_GOODS),
            key = {MQConst.ROUTING_GOODS_UPPER}
    ))
    public void goodsUpper(Long skuId, Message message, Channel channel){
        try {
            //判断
            if(skuId != null) {
                //
                listService.onSale(skuId);
            }
            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            //第三个参数 是否重回队列
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                //记录日记表 : skuId上架没有成功,
                //
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    //商品下架
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_GOODS_LOWER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_GOODS),
            key = {MQConst.ROUTING_GOODS_LOWER}
    ))
    public void goodsLower(Long skuId,Message message,Channel channel){
        try {
            //判断
            if(skuId != null) {
                //
                listService.cancelSale(skuId);
            }
            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            //第三个参数 是否重回队列
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                //可以做个记录日记表 : skuId上架没有成功, insert into skuId...
                //
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
