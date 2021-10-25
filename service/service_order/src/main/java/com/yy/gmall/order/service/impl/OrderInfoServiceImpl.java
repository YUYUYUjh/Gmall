package com.yy.gmall.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.cart.client.CartFeignClient;
import com.yy.gmall.common.util.HttpClientUtil;
import com.yy.gmall.model.enums.OrderStatus;
import com.yy.gmall.model.enums.ProcessStatus;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.order.mapper.OrderDetailMapper;
import com.yy.gmall.order.mapper.OrderInfoMapper;
import com.yy.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
        orderInfo.setTradeBody("订单描述");
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
            cartFeignClient.deleteCart(orderDetail.getSkuId());
        });

        //删除缓存中的流水号
        redisTemplate.delete("tradeNo:" + tradeNo);

        //返回orderId
        return orderId;
    }

    @Override
    public Boolean checkStock(Long skuId, Integer skuNum) {
        //远程调用库存系统判断商品库存
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(result);
    }

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
}
