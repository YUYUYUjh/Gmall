package com.yy.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.cart.client.CartFeignClient;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.service.RabbitService;
import com.yy.gmall.common.util.HttpClientUtil;
import com.yy.gmall.model.enums.OrderStatus;
import com.yy.gmall.model.enums.ProcessStatus;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.order.mapper.OrderDetailMapper;
import com.yy.gmall.order.mapper.OrderInfoMapper;
import com.yy.gmall.order.service.OrderInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yu
 * @create 2021-10-20 18:58
 */
@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private RabbitService rabbitService;

    //远程调用地址
    @Value("${ware.url}")
    private String wareUrl;

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo,String tradeNo) {
        //有些字段页面未传递,自己手动添加
        //订单金额
        orderInfo.sumTotalAmount();
        //订单进度
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //订单交易编号
        String outTradeNo = System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //订单描述
        //将所有的商品名称进行拼接
        StringBuilder sb = new StringBuilder();
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            sb.append(orderDetail.getSkuName());
        }
        if (sb.length()>200){
            orderInfo.setTradeBody(sb.substring(0,200));
        }else{
            orderInfo.setTradeBody(sb.toString());
        }
        //操作时间
        orderInfo.setOperateTime(new Date());
        //订单过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //订单进度状态
        //在订单的进度状态中可以获取订单状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        //插入orderInfo
        orderInfoMapper.insert(orderInfo);
        Long orderId = orderInfo.getId();

        //插入orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        orderDetailList.stream().forEach(orderDetail -> {
            //添加订单详情
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
            //删除购物车
            //mysql高级 ,  类型转换会导致索引失效  所以传入的字段和数据库的字段要保持数据一致
            //cartFeignClient.deleteCart(orderDetail.getSkuId()); //每次测试都需要添加购物车,不删了
        });
        //删除缓存中的流水号
        redisTemplate.delete("tradeNo:" + tradeNo);

        //开始计时 , 在规定时间内不付款 取消订单
        /* 方便测试, 注掉
        this.rabbitService.sendDelayedMessage(MQConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MQConst.ROUTING_ORDER_CANCEL,
                orderId.toString(),
                12);
*/
        //返回orderId
        return orderId;
    }

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public Boolean  checkStock(Long skuId, Integer skuNum) {
        //远程调用库存系统判断商品库存
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(result);
    }

    /**
     * 查看我的订单列表
     * @param pageParam
     * @param userId
     * @return
     */
    @Override
    public IPage<OrderInfo> getPage(Page<OrderInfo> pageParam, String userId) {
        //调用订单
        //需要的数据:  订单以及订单明细
        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectPageByUserId(pageParam,userId);

        //获取显示用的订单状态
        orderInfoIPage.getRecords().stream().forEach(orderInfo -> {
            orderInfo.setOrderStatusName(OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus()));
        });

        return orderInfoIPage;
    }

    /**
     * 取消订单: 其实就是修改订单状态 setOrderStatus
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId) {
        if (orderId > 0 && orderId != null) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(orderId);
            orderInfo.setOrderStatus(ProcessStatus.CLOSED.name());
            orderInfoMapper.updateById(orderInfo);
        }
    }

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoByOrderId(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        return orderInfo;
    }

    /**
     * 查询订单信息,包含订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        //获取orderInfo
        OrderInfo orderInfo = this.orderInfoMapper.selectById(orderId);
        //获取订单详情明细
        if (orderInfo!=null){
            List<OrderDetail> orderDetailList = this.orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));

            //赋值
            orderInfo.setOrderDetailList(orderDetailList);
        }
        return orderInfo;
    }

    /**
     * 修改订单状态,订单进度状态
     * @param orderId  订单id
     * @param status   订单状态
     */
    @Override
    public void updateOrderStatusById(Long orderId, ProcessStatus status) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(status.getOrderStatus().name());
        orderInfo.setProcessStatus(status.name());
        orderInfoMapper.updateById(orderInfo);
    }

    /**
     * 初始化仓库需要的数据
     * @param orderId
     * @return
     */
    @Override
    public Map initWareOrder(Long orderId) {
        //获取orderId对应的订单信息及订单明细
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        //封装数据
        Map orderMap = new HashMap();
        orderMap.put("orderId", orderInfo.getId());
        orderMap.put("consignee", orderInfo.getConsignee());
        orderMap.put("consigneeTel", orderInfo.getConsigneeTel());
        orderMap.put("orderComment", orderInfo.getOrderComment());
        orderMap.put("orderBody", orderInfo.getTradeBody());
        orderMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        orderMap.put("paymentWay", orderInfo.getPaymentWay().equals("ONLINE")?"2":"1");
        //orderMap.put("wareId", orderInfo.getWareId());// 仓库Id ，减库存拆单时需要使用！

        List<Map> orderDetailMapList = orderInfo.getOrderDetailList()
                .stream().map(orderDetail -> {
                    Map map = new HashMap();
                    map.put("skuId",orderDetail.getSkuId());
                    map.put("skuNum",orderDetail.getSkuNum());
                    map.put("skuName",orderDetail.getSkuName());
                    return map;
                }).collect(Collectors.toList());

        orderMap.put("details",orderDetailMapList);
        return orderMap;
    }

    @Override
    public Map initWareOrder(OrderInfo orderInfo) {
        //封装数据
        Map orderMap = new HashMap();
        orderMap.put("orderId", orderInfo.getId());
        orderMap.put("consignee", orderInfo.getConsignee());
        orderMap.put("consigneeTel", orderInfo.getConsigneeTel());
        orderMap.put("orderComment", orderInfo.getOrderComment());
        orderMap.put("orderBody", orderInfo.getTradeBody());
        orderMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        orderMap.put("paymentWay", orderInfo.getPaymentWay().equals("ONLINE")?"2":"1");
        orderMap.put("wareId", orderInfo.getWareId());// 仓库Id ，减库存拆单时需要使用！

        List<Map> orderDetailMapList = orderInfo.getOrderDetailList()
                .stream().map(orderDetail -> {
                    Map map = new HashMap();
                    map.put("skuId",orderDetail.getSkuId());
                    map.put("skuNum",orderDetail.getSkuNum());
                    map.put("skuName",orderDetail.getSkuName());
                    return map;
                }).collect(Collectors.toList());

        orderMap.put("details",orderDetailMapList);
        return orderMap;
    }

    /**
     * 拆单 : 一张订单拆分成多张订单 (按仓库拆分)
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    public List<OrderInfo> orderSplit(Long orderId, String wareSkuMap) {
        //获取父订单
        OrderInfo orderInfoOrigin = this.getOrderInfo(orderId);
        //开始拆分  以仓库为key
        // [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> wareSkuMapList = JSONObject.parseArray(wareSkuMap,Map.class);


        List<OrderInfo> subInfoList = wareSkuMapList.stream().map(map -> {
            //子订单
            OrderInfo subOrderInfo = new OrderInfo();
            //父订单的所有属性全部给子订单
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
            //父订单中没有的属性
            //Id
            subOrderInfo.setId(null);//数据库生成
            //parent_orderId
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());

            //仓库id
            subOrderInfo.setWareId(map.get("wareId").toString());
            // 总金额
            List<String> skuIds = (List<String>) map.get("skuIds");
            List<OrderDetail> subOrderDetailList = orderInfoOrigin.getOrderDetailList().stream().filter(orderDetail -> {
                if (skuIds.contains(String.valueOf(orderInfoOrigin.getId()))) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            subOrderInfo.sumTotalAmount(); //父订单有,但是不对

            //保存子订单到DB中 返回值是订单id
            orderInfoMapper.insert(subOrderInfo);
            //子订单详情的外键
            subOrderDetailList.forEach(suborderDetail -> {
                suborderDetail.setOrderId(subOrderInfo.getId());
                orderDetailMapper.updateById(suborderDetail);
            });
            return subOrderInfo;
        }).collect(Collectors.toList());

        //把父订单的订单状态改为已拆单,进度状态
        this.updateOrderStatusById(orderInfoOrigin.getId(),ProcessStatus.SPLIT);

        return subInfoList;
    }


}
