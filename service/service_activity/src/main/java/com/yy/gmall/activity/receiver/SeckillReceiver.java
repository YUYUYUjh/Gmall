package com.yy.gmall.activity.receiver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.yy.gmall.activity.mapper.SeckillGoodsMapper;
import com.yy.gmall.activity.service.SeckillGoodsService;
import com.yy.gmall.activity.util.CacheHelper;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.util.DateUtil;
import com.yy.gmall.common.util.MD5;
import com.yy.gmall.model.activity.OrderRecode;
import com.yy.gmall.model.activity.SeckillGoods;
import com.yy.gmall.model.activity.UserRecode;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu
 * @create 2021-11-01 18:49
 *
 */
@Component
public class SeckillReceiver {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    //MQConst.EXCHANGE_DIRECT_TASK,MQConst.ROUTING_TASK_1

    /**
     * 监听秒杀商品上架消息
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_TASK_1,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_TASK),
            key = {MQConst.ROUTING_TASK_1}
    ))
    public void insertSeckillGoodsToRedis(Message message, Channel channel){
        try {
            //将当天的秒杀商品放入缓存(从数据库查出秒杀商品,放入redis,只查当天的)
            /*
            SELECT *
            FROM `seckill_goods`
            WHERE `status` = 1 AND DATE_FORMAT(start_time,'%Y-%m-%d') = '2021-11-01'
             */
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("status","1");
            wrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
            //获取到当天秒杀的商品列表
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);
            //将当天的秒杀商品集合放入redis
            //注意事项:不要存入int,long类型,存入string类型
            seckillGoodsList.stream().forEach(seckillGoods -> {
                //使用hash key=seckill:goods field=skuId value=secKillGoods
                Boolean putIfAbsent = redisTemplate.opsForHash().putIfAbsent(RedisConst.SECKILL_GOODS, seckillGoods.getSkuId().toString(), seckillGoods);
                //如果put成功了再添加秒杀商品,必须保证不要叠加秒杀商品数量
                if (putIfAbsent){

                    //将每个商品对应的库存剩余数放入redis中
                    //使用list类型,
                    // 要秒杀的商品库存有多少个就push多少次,售卖一次就pop一次,pop没了键就为null,直接表示商品售光
                    // key = seckill:stock:skuId
                    for (Integer i = 0; i < seckillGoods.getStockCount(); i++) {
                        String key = RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId();
                        redisTemplate.opsForList().leftPush(key,seckillGoods.getSkuId().toString());
                    }
                    //秒杀商品在初始化的时候,状态为初始化为1
                    //使用redis推送通知,
                    //PUBLISH channel message 将消息发送到指定频道, message为商品Id:状态位(1or0)
                    //  publish seckillpush 46:1  | 后续业务如果说商品被秒杀完了！ publish seckillpush 46:0
                    redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");
                    System.out.println("SeckillReceiver监听到秒杀商品上架");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * 监听开始抢购
     *
     * @param message
     * @param channel
     * @throws
     */
    //接收消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SECKILL_USER),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = MQConst.ROUTING_SECKILL_USER
    ))
    public void seckill(Message message, Channel channel, UserRecode userRecode) throws Exception {
        Long skuId = userRecode.getSkuId();
        String userId = userRecode.getUserId();
        String state = (String) CacheHelper.get(skuId.toString());
        //1:再次判断产品状态位， 1：可以秒杀 0：秒杀结束
        if(!"1".equals(state)){
            //已售罄 应答 队列删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //2: 校验用户是否来过 key = seckill:user:userId
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER +
                        userId + ":" + skuId,"",RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if(isExist){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;//此用户已经买过了、不能再买了
        }
        //3: 获取队列中的商品，如果能够获取，则商品存在，可以下单
        String id = (String) redisTemplate.opsForList().
                rightPop(RedisConst.SECKILL_STOCK_PREFIX + skuId);
        if(StringUtils.isEmpty(id)){
            //商品售罄，更新状态位
            redisTemplate.convertAndSend("seckillpush", skuId + ":0");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //4：可以下单了
        this.seckillGoodsService.saveOrder(userId,skuId);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}












