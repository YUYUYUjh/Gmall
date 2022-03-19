package com.yy.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.entity.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Yu
 * @create 2021-10-24 15:05
 * @Description MQ消息应答实现配置类
 * <p>
 * ConfirmCallback  只确认消息是否正确到达 Exchange 中
 * ReturnCallback   消息没有正确到达队列时触发回调，如果正确到达队列不执行
 * <p>
 * 1. 如果消息没有到exchange,则confirm回调,ack=false
 * 2. 如果消息到达exchange,则confirm回调,ack=true
 * 3. exchange到queue成功,则不回调return
 * 4. exchange到queue失败,则回调return
 *
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    /**
     * spring实例化过程:
     * 1.MQProducerAckConfig被实例化  @Component  放入springIOC容器中
     * 2.构造器
     * 3. @Autowired 注入
     *      问题: 先走构造器的时候还没有RabbitTemplate , 会报空指针异常
     *      解决: 使用后置构造器 , 先bean实例化,注入,再构造器
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    //注入  , 等于配置文件bean中的ref
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 判断消息是否正确到交换机    成功也应答 , 失败也应答
     * @param correlationData 可以接收用户发送过来的消息,同时在这个类中有一个Id唯一标识! 扩展发送消息时需要携带额外的属性
     * @param ack   是否成功
     * @param cause 失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            //成功应答
            log.info("交换机接收消息成功：" + JSON.toJSONString(correlationData));
        } else {
            //失败应答
            log.info("交换机接收消息失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));
            //重新发消息3次, 指定次数 , 不允许无限消息
            //如果指定次数还是发送不成功 , 打日志转人工处理
            reTrySendMessage(correlationData);
            //重发需要:1.交换机 2.RoutingKey 3.消息 4.重发几次
        }
    }

    /**
     * 重新发消息
     * 问题: 想要的数据没有
     */
    private void reTrySendMessage(CorrelationData correlationData) {

        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;

        //重新发消息  可以一直重新发送, 但是不允许,需要判断发送几次了
        //判断发送次数  不能超过3次
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount < 3) {
            retryCount++; // 0  1  2  总共3次
            gmallCorrelationData.setRetryCount(retryCount);
            //更新Redis缓存中的次数
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),JSONObject.toJSON(gmallCorrelationData));

            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),
                    gmallCorrelationData.getRoutingKey(),
                    gmallCorrelationData.getMessage(),
                    gmallCorrelationData);


        }else {
            //发送的次数已经耗尽了
            // {} 相当于 ?占位符  把JSONObject.toJSONString(gmallCorrelationData)填充到{}的位置
            log.error("发送次数已耗尽:{}:{}", JSONObject.toJSONString(gmallCorrelationData),"哈哈");
        }
    }

    /**
     * 消息是否正确发送到队列   只有失败才应答
     * @param message 消息主体
     * @param replyCode 应答码
     * @param replyText 应答描述
     * @param exchange  消息使用的交换器
     * @param routingKey    消息使用的路由键
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {



        //判断是否为插件延时消息
        if (MQConst.EXCHANGE_DIRECT_ORDER_CANCEL.equals(exchange)){
            return;
        }

        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);

        //队列应答失败 重新发送消息3次

        //方法1. 使用外力 , 使用redis缓存
        //方法2. Message

        //使用Redis获取gmallCorrelationData,
        String uuid = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        Object o = redisTemplate.opsForValue().get(uuid);
        GmallCorrelationData gmallCorrelationData = JSONObject.parseObject(o.toString(), GmallCorrelationData.class);
        //准备出关联数据对象


        reTrySendMessage(gmallCorrelationData);

    }
}
