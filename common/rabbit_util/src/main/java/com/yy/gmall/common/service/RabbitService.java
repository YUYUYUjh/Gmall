package com.yy.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.gmall.common.config.DelayedMqConfig;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu
 * @create 2021-10-24 15:15
 *
 * 自定义实现类 简化开发
 */
ppressWarnings("all")
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     *
     * 要求 : 发送失败后可以在交换机 队列应答处 可以重新发送消息
     *          convertAndSend用4个参数的方法
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {

        //CorrelationData correlationData = new CorrelationData();

        //CorrelationData 不够需要的数据  给correlationData 扩展几个成员变量

        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        //交换机
        gmallCorrelationData.setExchange(exchange);
        //RoutingKey
        gmallCorrelationData.setRoutingKey(routingKey);
        //message
        gmallCorrelationData.setMessage(message);

        //使用redis保存gmallCorrelationData
        // 如何保证key唯一且能被returnedMessage回调到 : 生成一个uuid,set到CorrelationData,然后从Message获取
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        gmallCorrelationData.setId(uuid);
        redisTemplate.opsForValue().set(uuid, JSON.toJSONString(gmallCorrelationData), 10   , TimeUnit.MINUTES);
        //重发消息需要4个参数的方法 , correlationData 发送失败之后  将此correlationData返回到交换机,队列应答处
        rabbitTemplate.convertAndSend(exchange, routingKey, message,gmallCorrelationData);
        return true;
    }

    //发送基于死信的延迟消息
    public void sendDeadLetterMessage(String exchange, String routingKey, Object message,int ttl){
        //关联数据对象
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        //交换机
        gmallCorrelationData.setExchange(exchange);
        //RoutingKey
        gmallCorrelationData.setRoutingKey(routingKey);
        //message
        gmallCorrelationData.setMessage(message);
        //是否为延迟消息
        gmallCorrelationData.setDelay(true);
        //延迟时长
        gmallCorrelationData.setDelayTime(ttl);
        //使用redis保存gmallCorrelationData
        // 如何保证key唯一且能被returnedMessage回调到 : 生成一个uuid,set到CorrelationData,然后从Message获取
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        gmallCorrelationData.setId(uuid);
        redisTemplate.opsForValue().set(uuid, JSONObject.toJSON(gmallCorrelationData));

        rabbitTemplate.convertAndSend(exchange,routingKey,message,message1 -> {
            //设置ttl的时间
            message1.getMessageProperties().setDelay(ttl*1000);
            return message1;
        },gmallCorrelationData);
    }
    //发送基于插件的延迟队列
    public void sendDelayedMessage(String exchange, String routingKey, Object msg,int ttl){
        //关联数据对象
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        //交换机
        gmallCorrelationData.setExchange(exchange);
        //RoutingKey
        gmallCorrelationData.setRoutingKey(routingKey);
        //message
        gmallCorrelationData.setMessage(msg);
        //是否为延迟消息
        gmallCorrelationData.setDelay(true);
        //延迟时长
        gmallCorrelationData.setDelayTime(ttl);
        //使用redis保存gmallCorrelationData
        // 如何保证key唯一且能被returnedMessage回调到 : 生成一个uuid,set到CorrelationData,然后从Message获取
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        gmallCorrelationData.setId(uuid);
        redisTemplate.opsForValue().set(uuid, JSONObject.toJSON(gmallCorrelationData));
        //设置过期时间
        this.redisTemplate.expire(uuid,24, TimeUnit.HOURS);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("发送消息的时间:"+sdf.format(new Date())+"----发送的数据:"+msg);
        this.rabbitTemplate.convertAndSend(exchange, routingKey,msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(ttl * 1000);
                return message;
            }
        },gmallCorrelationData);
    }
}
