package com.yy.gmall.order.config;

import com.yy.gmall.common.constant.MQConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-26 21:31
 * 将交换机与队列绑定
 */
@Configuration
public class OrderDelayedMqConfig {

    //交换机
    @Bean
    public CustomExchange orderDelayExchange() {
        //x-delayed-type : CustomExchange自定义交换机的类型 , 交换机类型
        //x-delayed-message: 交换机中延迟消息的类型  ,  交换机中消息的类型
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MQConst.EXCHANGE_DIRECT_ORDER_CANCEL, "x-delayed-message", true, false, args);
    }
    //队列
    @Bean
    public Queue orderDelayQueue() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(MQConst.QUEUE_ORDER_CANCEL, true);
    }
    //绑定
    @Bean
    public Binding delayBinding3(@Qualifier(value = "orderDelayQueue") Queue delayQueue,
                                  @Qualifier(value = "orderDelayExchange") CustomExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(MQConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
