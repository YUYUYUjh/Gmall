package com.yy.gmall.activity.controller;

import com.yy.gmall.activity.service.SeckillGoodsService;
import com.yy.gmall.activity.util.CacheHelper;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.result.ResultCodeEnum;
import com.yy.gmall.common.service.RabbitService;
import com.yy.gmall.common.util.AuthContextHolder;
import com.yy.gmall.common.util.DateUtil;
import com.yy.gmall.common.util.MD5;
import com.yy.gmall.model.activity.OrderRecode;
import com.yy.gmall.model.activity.SeckillGoods;
import com.yy.gmall.model.activity.UserRecode;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.product.client.ProductFeignClient;
import com.yy.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Yu
 * @create 2021-11-01 21:57
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private UserFeignClient userFeignClient;


    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 查询所有秒杀商品
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result findAll() {
        return Result.ok(seckillGoodsService.findAll());
    }

    /**
     * 按skuId查询秒杀商品
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable("skuId") Long skuId) {
        return Result.ok(seckillGoodsService.getSeckillGoods(skuId));
    }

    /**
     * 获取下单码
     * @param skuId
     * @param request
     * @return
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        if (null != seckillGoods) {
            Date curTime = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), curTime) && DateUtil.dateCompare(curTime, seckillGoods.getEndTime())) {
                //可以动态生成，放在redis缓存
                String skuIdStr = MD5.encrypt(userId);
                return Result.ok(skuIdStr);
            }
        }
        return Result.fail().message("获取下单码失败");
    }

    /**
     * 点击开始抢购
     * @param skuId
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) throws Exception {
        //1.校验下单码（抢购码规则可以自定义）
        // 前面我们的下单码的生成方式: 对当前用户id进行md5加密
        // 1.1 获取当前用户id , 获取skuIdStr ,
        String userId = AuthContextHolder.getUserId(request);
        String skuIdStr = request.getParameter("skuIdStr");
        // 1.2 对当前用户id进行加密,和skuIdStr对比判断
        if (!skuIdStr.equals(MD5.encrypt(userId))) {
            //下单码不一致
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }

        //2.查看状态位 , 判断是否有商品
        //key=skuId， value=状态
        String state = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(state)) {
            //2.1状态位为空  请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("1".equals(state)) {
            //2.2 状态位value为1,代表有货,可以下单
            //封装下单所需数据
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            //发送下单消息 , 传递下单所需数据: 用户id和商品id  //使用mq是为了解决高并发
            rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_SECKILL_USER, MQConst.ROUTING_SECKILL_USER, userRecode);
        } else {
            //2.3已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        return Result.ok();
    }

    /**
     * 查询秒杀订单状态
     * @return
     */
    @GetMapping(value = "auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //当前登录用户
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        return seckillGoodsService.checkOrder(skuId, userId);
    }

    /**
     * 秒杀确认订单
     * @param request
     * @return
     */
    //下单
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request){

        String userId = AuthContextHolder.getUserId(request);

        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));

        OrderRecode orderRecode = (OrderRecode) redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDERS
                + userId);
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        // 声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        // 添加到集合
        detailArrayList.add(orderDetail);

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalAmount", orderDetail.getOrderPrice());
        result.put("totalNum", orderDetail.getSkuNum());
        return Result.ok(result);
    }
}
