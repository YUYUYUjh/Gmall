package com.yy.gmall.task.scheduled;

import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Yu
 * @create 2021-11-01 18:39
 */
@Component
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    //@Scheduled(cron = "0 0 1 * * ?")  //每条凌晨1点执行
    @Scheduled(cron = "0/5 * * * * ?") //测试,每5秒执行一次
    public void task(){
        rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_TASK,MQConst.ROUTING_TASK_1,"");
        System.out.println("发起查询缓存定时任务---时间:"+new Date());
    }
}
