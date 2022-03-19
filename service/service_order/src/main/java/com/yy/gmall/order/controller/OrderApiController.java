package com.yy.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.cart.client.CartFeignClient;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.util.AuthContextHolder;
import com.yy.gmall.model.cart.CartInfo;
import com.yy.gmall.model.order.OrderDetail;
import com.yy.gmall.model.order.OrderInfo;
import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.order.service.OrderInfoService;
import com.yy.gmall.product.client.ProductFeignClient;
import com.yy.gmall.user.client.UserFeignClient;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Yu
 * @create 2021-10-19 19:32
 */
@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 点击购物车页面的结算跳转提交订单trade页面
     * @param request
     * @return
     */
    @GetMapping("/auth/trade")
    public Map<String,Object> trade(HttpServletRequest request){

        //获取用户id , 走进auth的请求肯定是登录的
        String reqeustId = AuthContextHolder.getUserId(request);
        Long userId = Long.parseLong(reqeustId);
        //将所有需要的数据封装进map
        Map<String,Object> map = new HashMap();

        /**
         *  trade页面需要的数据:
         *      1.用户信息  userInfo
         *      2.用户地址  userAddress
         *      3.购物清单  detailArrayList
         *      4.选中商品 List<CartInfo>
         */
        //用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        map.put("userAddressList",userAddressList);
        //选中商品
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        //循环遍历给OrderDetail赋值
        //商品总数量
        AtomicInteger totalNum = new AtomicInteger();
        List<OrderDetail> detailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            totalNum.addAndGet(cartInfo.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());
        map.put("detailArrayList",detailArrayList);
        //totalNum 总件数
//        int total = cartCheckedList.stream().mapToInt(CartInfo::getSkuNum).sum();
        map.put("totalNum",totalNum);
        //totalAmount 总金额 使用stream+lambda+reduce聚合函数
        BigDecimal totalAmount = detailArrayList.stream()
                .map(orderDetail -> orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum().toString())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //BigDecimal reduce1 = list.stream().map(a->a.getBeginBalance().multiply(a.getRate())).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal totalAmount = detailArrayList.stream().map(o -> o.getOrderPrice().multiply(new BigDecimal(o.getSkuNum().toString()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        map.put("totalAmount",totalAmount);

        //订单流水号 , 防止用户重复提交订单
        String tradeNo = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("tradeNo:"+tradeNo,userId);
        redisTemplate.expire("tradeNo:"+tradeNo,7, TimeUnit.DAYS);
        map.put("tradeNo",tradeNo);
        return map;
    }

    /**
     * 提交订单
     * @RequestBody:接收前端传递的对象
     */
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request){
        String tradeNo = request.getParameter("tradeNo");
        if (CollectionUtils.isEmpty(orderInfo.getOrderDetailList())){
            return Result.fail().message("订单无商品");
        }
        //防止用户提交订单以后,回退再次提交订单
        /*方法:
          在用户点击结算的时候生成唯一的订单流水号,放入缓存
          在用户点击提交订单的时候,将唯一的流水号从缓存删除,
          如果用户回退再次提交订单,缓存中的唯一流水号已经没了,就无法再次提交
         */
        //判断订单流水号
        //如果订单流水号为空
        if (StringUtils.isEmpty(tradeNo)){
            return Result.fail().message("订单无效");
        }
        //从redis查订单流水号,判断是否存在
        Object redisTradeNo = redisTemplate.opsForValue().get("tradeNo:" + tradeNo);
        if (redisTradeNo == null){
            return Result.fail().message("请勿重复提交订单");
        }
        //走到这有订单流水号
        //获取用户id
        String id = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(id));
        //判断存储的订单流水号的对应的value是否相同
        if (id.equals(redisTradeNo.toString())){
            //id也相同
            //使用异步编排1.查询库存 , 判断每个商品的库存数量
            //             2.查询最新价格,判断每个商品的最新价格和购物车是否出入
            //声明一个异步编排集合
            List<CompletableFuture> futureList = new ArrayList<>();
            //声明一个集合接收错误,如果异步编排线程出错了就添加一次错误集合,
            // 最后判断错误集合长度,如果长度大于1说明有问题,就return 失败
            List<String> errorList = new ArrayList<>();
            for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
                //验证库存
                CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                    Boolean res = orderInfoService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                    if (!res) {
                        errorList.add(orderDetail.getSkuName() + "库存不足!");
                    }
                }, threadPoolExecutor);
                //将这个对象添加到集合
                futureList.add(stockCompletableFuture);
                //验证价格
                CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    if (skuPrice.compareTo(orderDetail.getOrderPrice()) != 0) {
                        //不等于0说明价格变动
                        //将最新价格放入缓存
                        //先从缓存查出价格变动的购物项
                        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(RedisConst.USER_KEY_PREFIX + id + RedisConst.USER_CART_KEY_SUFFIX, orderDetail.getSkuId().toString());
                        //将最新价格塞入购物项
                        cartInfo.setSkuPrice(skuPrice);
                        //将购物项重新塞入redis
                        redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + id + RedisConst.USER_CART_KEY_SUFFIX, orderDetail.getSkuId().toString(), cartInfo);
                        //返回信息
                        errorList.add(orderDetail.getSkuName() + "价格有变动!请到购物车重新结算");
                    }
                }, threadPoolExecutor);
                //将这个对象添加到集合
                futureList.add(priceCompletableFuture);
            }
            //任务组合
            //CompletableFuture.allOf(需要一个数组),集合变数组toArray
            //toArray(需要一个泛型数组(CompletableFuture[长度就是futureList的长度]))
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));

            //判断是否有错误
            if (errorList.size()>0){
                return Result.fail().message(StringUtils.join(errorList,","));
            }

            //未使用异步编排验证库存和价格
            //            for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //                //1.验证库存
            //                Boolean res = orderInfoService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            //                if (!res){
            //                    return Result.fail().message(orderDetail.getSkuName()+"库存不足!");
            //                }
            //                //2.验证最新价格
            //                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
            //                if (skuPrice.compareTo(orderDetail.getOrderPrice())!=0){
            //                    //不等于0说明价格变动
            //                    //将最新价格放入缓存
            //                    //先从缓存查出价格变动的购物项
            //                    CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(RedisConst.USER_KEY_PREFIX + id + RedisConst.USER_CART_KEY_SUFFIX, orderDetail.getSkuId().toString());
            //                    //将最新价格塞入购物项
            //                    cartInfo.setSkuPrice(skuPrice);
            //                    //将购物项重新塞入redis
            //                    redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX+id+RedisConst.USER_CART_KEY_SUFFIX,orderDetail.getSkuId().toString(),cartInfo);
            //                    //返回信息
            //                    return Result.fail().message(orderDetail.getSkuName()+"价格有变动!请到购物车重新结算");
            //                }
            //            }
            //生成订单
            // 为了保证事务,从缓存删除订单流水号在保存订单业务中执行
            Long orderId = orderInfoService.saveOrderInfo(orderInfo,tradeNo);
            return Result.ok(orderId);
        }
        return Result.fail().message("订单提交失败");
    }

    /**
     * 查看我的订单
     * @param page
     * @param limit
     * @param request
     * @return
     */
    @GetMapping("auth/{page}/{limit}")
    public Result<IPage<OrderInfo>> orderList(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);

        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> orderInfoIPage = orderInfoService.getPage(pageParam,userId);
        return Result.ok(orderInfoIPage);
    }

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("/auth/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(orderId);
        return orderInfo;
    }
    /**
     * 查询订单信息包含订单明细
     * @param orderId
     * @return
     */
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfoAndOrderDetail(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return orderInfo;

    }

    /**
     * 仓库微服务发起,远程调用订单服务,要求订单微服务进行拆弹
     */
    @PostMapping("/orderSplit")
    public String orderSplit(Long orderId,String wareSkuMap){
        // 拆单：获取到的子订单集合
        List<OrderInfo> subOrderInfoList = orderInfoService.orderSplit(orderId,wareSkuMap);
        // 声明一个存储map的集合 返回给仓库微服务
        ArrayList<Map> mapArrayList = new ArrayList<>();
        // 生成子订单集合
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderInfoService.initWareOrder(orderInfo);
            // 添加到集合中！
            mapArrayList.add(map);
        }
        return JSON.toJSONString(mapArrayList);
    }

}
