package com.yy.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import com.yy.gmall.common.config.DeadLetterMqConfig;
import com.yy.gmall.common.config.DelayedMqConfig;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ConfirmReceiver {

    //监听发送的消息! product->exchange->routingKey->queue->consumer
    @RabbitListener(bindings=@QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm",autoDelete = "true"),
            key = {"routing.confirm"}))
    public void process(Message message, Channel channel) throws IOException {
        //接收消息的方法
        //消息对象的组成:
        //  1.消息的属性对象(1.1 消息头 1.2其他属性组成)
        //
        System.out.println("RabbitListener:"+new String(message.getBody()));

        // 采用手动应答模式, 手动确认应答更为安全稳定
        //如果手动确定了，再出异常，mq不会通知；如果没有手动确认，抛异常mq会一直通知
        //第一个参数:表示消息唯一  第二个参数:表示是否批量确认
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            int i = 1/0;
            // false 确认一个消息，true 批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 消息是否再次被拒绝！
            // getRedelivered() 判断是否已经处理过一次消息！
            if (message.getMessageProperties().getRedelivered()) {
                System.out.println("消息已重复处理,拒绝再次接收");
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                System.out.println("消息即将再次返回队列处理");
                //参数一: 消息的标识符  参数二：是否批量， 参数三：为是否重新回到队列，true重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

    // mq内部  监听死信消息
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getQueueDead(String msg,Channel channel,Message message){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("接收的时间:"+sdf.format(new Date()));
        System.out.println("接收到的消息"+new String(message.getBody()));

        //手动确认,
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    // 插件延迟队列  监听死信消息
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getQueueDead1(String msg,Channel channel,Message message){
        byte[] body = message.getBody();
        String orderId = new String(body);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("接收的时间:"+sdf.format(new Date())+"----------"+"接收到的消息"+new String(message.getBody()));

        //手动确认,
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}