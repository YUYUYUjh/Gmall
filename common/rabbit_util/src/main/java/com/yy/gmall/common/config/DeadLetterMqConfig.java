package com.yy.gmall.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 基于mq内部实现延迟队列
 */
@Configuration
public class DeadLetterMqConfig {

    //定义一些变量
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    //定义交换机
    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchange_dead,true,false);
    }
    //定义队列
    @Bean
    public Queue queue(){
        HashMap map = new HashMap();
        map.put("x-dead-letter-exchange", exchange_dead);
        map.put("x-dead-letter-routing-key", routing_dead_2);
        map.put("x-message-ttl", 10 * 1000);

        return new Queue(queue_dead_1,true,false,false,map);
    }
    //定义绑定  交换机--通过--路由键1--绑定--队列1
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(exchange()).with(routing_dead_1);
    }

    //定义队列2
    @Bean
    public Queue queue2(){
        return new Queue(queue_dead_2,true,false,false);
    }

    //定义绑定  交换机--通过--路由键2--绑定--队列2
    @Bean
    public Binding binding2(){
        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }
}