package com.yy.gmall.activity.redis;

import com.yy.gmall.activity.util.CacheHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Yu
 * @create 2021-11-01 20:46
 *
 * 自定义实现类:
 *  作用: 当监听器接收到redis服务器中消息的时候,对消息是如何处理 就在当前类中写具体操作
 */
@Component
public class MessageReceive {

    //接收消息的方法
    public void receiveMessage(String message){
        System.out.println("Redis监听器----收到了消息message:"+message);
        if (!StringUtils.isEmpty(message)){
            /*
                消息格式:
                    skuId:0 表示没有商品
                    skuId:1 表示有商品
             */
            //传递过来的数据为 ""6:1""
            message = message.replace("\"","");
            //替换以后 message 为 6:1
            //切割 0角标是商品id , 1角标是商品状态
            String[] split = StringUtils.split(message, ":");
            if (split == null || split.length ==2 ){
                //将状态位放入本地缓存
                CacheHelper.put(split[0],split[1]);
            }
        }
    }
}
