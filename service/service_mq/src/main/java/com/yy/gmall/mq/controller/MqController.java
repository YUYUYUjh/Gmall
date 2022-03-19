package com.yy.gmall.mq.controller;

import com.yy.gmall.common.config.DeadLetterMqConfig;
import com.yy.gmall.common.config.DelayedMqConfig;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mq")
@Slf4j
public class MqController {


   @Autowired
   private RabbitService rabbitService;

   @Autowired
   private RabbitTemplate rabbitTemplate;


   /**
    * 消息发送
    */
   //http://cart.gmall.com:8282/mq/sendConfirm
   @GetMapping("/sendConfirm/{message}")
   public Result sendConfirm(@PathVariable String message) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      rabbitService.sendMessage("exchange.confirm",
              "routing.confirm",
              message);
      return Result.ok();
   }

   @GetMapping("/sendDeadLettle")
   public Result sendDeadLettle(){
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      System.out.println("发送的时间:"+sdf.format(new Date()));
      this.rabbitService.sendDeadLetterMessage(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"hello",15);
      return Result.ok();
   }

   //发送基于插件的延迟消息
   @GetMapping("/sendDelay/{message}/{time}")
   public Result sendDelay(@PathVariable String message,
                           @PathVariable int time){
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      System.out.println("发送的时间:"+sdf.format(new Date())+"----"+message);
      //基于插件需要使用原生rabbitTemplate
      this.rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay,message, new MessagePostProcessor() {
         @Override
         public Message postProcessMessage(Message message) throws AmqpException {
            message.getMessageProperties().setDelay(time * 1000);
            System.out.println(sdf.format(new Date()) + " Delay sent.");
            return message;
         }
      });
      return Result.ok();
   }
}