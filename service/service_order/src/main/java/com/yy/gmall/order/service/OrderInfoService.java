package com.yy.gmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.model.enums.ProcessStatus;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

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

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId);

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoByOrderId(Long orderId);

    /**
     * 查询订单信息包含订单详情信息
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * 更新订单状态
     */
    void updateOrderStatusById(Long orderId, ProcessStatus status);

    /**
     * 初始化仓库系统需要的数据
     * @param orderId
     * @return
     */
    Map initWareOrder(Long orderId);

    /**
     * 初始化仓库系统需要的数据
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆弹
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(Long orderId, String wareSkuMap);
}
