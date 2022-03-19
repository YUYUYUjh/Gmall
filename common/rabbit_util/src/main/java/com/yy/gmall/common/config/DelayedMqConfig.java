package com.yy.gmall.common.config;

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
 * 基于插件实现延迟队列
 */
@Configuration
public class DelayedMqConfig {

    //一个交换机 , 一个队列 , 一个路由键绑定
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    //交换机
    @Bean
    public CustomExchange delayExchange() {
        //x-delayed-type : CustomExchange自定义交换机的类型 , 交换机类型
        //x-delayed-message: 交换机中延迟消息的类型  ,  交换机中消息的类型
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, args);
    }
    /**
     * 队列不要在RabbitListener上面做绑定，否则不会成功，如队列2，必须在此绑定
     *
     * @return
     */
    @Bean
    public Queue delayQueue1() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(queue_delay_1, true);
    }
    //绑定
    @Bean
    public Binding delayBbinding1(@Qualifier(value = "delayQueue1") Queue delayQueue,
                                  @Qualifier(value = "delayExchange") CustomExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(routing_delay).noargs();
    }

}