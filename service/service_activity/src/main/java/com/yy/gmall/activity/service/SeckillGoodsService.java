package com.yy.gmall.activity.service;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

   /**
    * 返回全部列表
    * @return
    */
   List<SeckillGoods> findAll();
   
   /**
    * 根据ID获取秒杀商品
    * @param id
    * @return
    */
   SeckillGoods getSeckillGoods(Long id);

   /***
    * 根据商品id与用户ID查看订单信息
    * @param skuId
    * @param userid
    * @return
    */
   Result checkOrder(Long skuId, String userid);


   /**
    * 保存秒杀订单
    * @param userId
    * @param skuId
    */
   void saveOrder(String userId, Long skuId);
}