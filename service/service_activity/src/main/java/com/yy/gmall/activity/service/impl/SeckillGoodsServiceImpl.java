package com.yy.gmall.activity.service.impl;

import com.alibaba.nacos.common.util.Md5Utils;
import com.yy.gmall.activity.mapper.SeckillGoodsMapper;
import com.yy.gmall.activity.service.SeckillGoodsService;
import com.yy.gmall.activity.util.CacheHelper;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.result.ResultCodeEnum;
import com.yy.gmall.common.util.MD5;
import com.yy.gmall.model.activity.OrderRecode;
import com.yy.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部秒杀商品
     */
    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return seckillGoodsList;
    }

    /**
     * 根据ID获取秒杀商品
     * @param id
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(id.toString());
    }

    /***
     * 轮询查看用户的秒杀商品状态
     * @param userId
     * @return
     */
    @Override
    public Result checkOrder(Long skuId, String userId) {
        //校验状态位是否有货
        String state = (String) CacheHelper.get(skuId.toString());
        if (!"1".equals(state)){
            //无货
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);// 213 售罄 抢单失败
        }

        //1.用户是否已经下单 //抢购成功
        Object o = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDERS_USERS + userId + ":" + skuId.toString());
        if (null != o ){
            return Result.build(null,ResultCodeEnum.SECKILL_ORDER_SUCCESS); //218 下单成功
        }
        //2.用户是否来过
        Boolean flag = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId + ":" + skuId);
        if (flag) {
            //来过
            //3.用户是否预下单成功  //抢购成功去下单
            Boolean flag2 = redisTemplate.hasKey(RedisConst.SECKILL_ORDERS + userId + ":" + skuId.toString());
            if (flag2){
                //预下单成功
                return Result.build(null,ResultCodeEnum.SECKILL_SUCCESS);// 215 抢单成功
            }
        }

        //4.用户排队中 //排队中
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);//211 排队中
    }

    /**
     * 保存秒杀预订单
     * @param userId
     * @param skuId
     */
    @Override
    @Transactional
    public void saveOrder(String userId, Long skuId) {
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS, skuId.toString());
        orderRecode.setSeckillGoods(seckillGoods);
        orderRecode.setOrderStr(MD5.encrypt(userId+skuId));
        //将下单信息保存到缓存 key=seckill:user:1:1 , value=orderRecode
        //保存预订单
        redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDERS + userId + ":" +skuId.toString(),orderRecode);
        //更新商品的库存
        this.updateStockCount(skuId);
    }

    /**
     * 更新秒杀库存数
     * @param skuId
     */
    //更新商品的库存
    private void updateStockCount(Long skuId) {

        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            //1.获取缓存商品数量
            Long size = redisTemplate.opsForList().size(RedisConst.SECKILL_STOCK_PREFIX + skuId);
            //2.更新数据库, 但是要做一个限制,不要每次都去操作数据库
            if(size%2 == 0){
                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash()
                        .get(RedisConst.SECKILL_GOODS,skuId.toString());
                seckillGoods.setStockCount(size.intValue());
                //更新Mysql数据库的库存
                seckillGoodsMapper.updateById(seckillGoods);
                //更新缓存中的库存
                redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS,skuId.toString(),seckillGoods);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}