package com.yy.gmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.order.OrderInfo;

/**
 * @author Yu
 * @create 2021-10-20 18:57
 */
public interface OrderInfoService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo,String tradeNo);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    Boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 查看我的订单列表
     */
    IPage<OrderInfo>  getPage(Page<OrderInfo> pageParam,String userId);
}
