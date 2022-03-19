package com.yy.gmall.cart.service.impl;

import com.yy.gmall.cart.mapper.CartInfoMapper;
import com.yy.gmall.cart.service.CartInfoAsyncService;
import com.yy.gmall.cart.service.CartService;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.model.cart.CartInfo;
import com.yy.gmall.model.product.SkuInfo;
import com.yy.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Yu
 * @create 2021-10-17 21:41
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CartInfoAsyncService cartInfoAsyncService;

    /**
     * 添加购物车实现
     * @param skuId
     * @param skuNum
     * @param userId
     */
    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        System.out.println("添加购物车主线程开启"+Thread.currentThread().getName());
        // 有购物车修改
        // 无购物车添加
        //获取购物车key
        String cartKey = this.getCartKey(userId);
        //查询此id的购物车是否存在
        CartInfo cartInfoExist = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());

        //判断
        if (cartInfoExist!=null){
            //不为空 更新操作  更新商品数量,实时价格
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //实时查询价格
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            cartInfoExist.setSkuPrice(price);
            //细节  如果第一次添加购物车以后把选中取消了,再添加购物车要手动设置选中
            cartInfoExist.setIsChecked(1);
            this.redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            //异步保存到数据库
            cartInfoAsyncService.updateAsyncCart(cartInfoExist);
        }else{
            //插入操作
            cartInfoExist = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
            cartInfoExist.setId(new Date().getTime());
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setCartPrice(skuInfo.getPrice());
            cartInfoExist.setSkuPrice(skuInfo.getPrice());
            cartInfoExist.setSkuNum(skuNum);
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            //
            this.redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
            //保存到DB
            cartInfoAsyncService.insertAsyncCart(cartInfoExist);
        }
        //过期时间  设置30天
        this.setCartKeyExpire(cartKey);
        System.out.println("添加购物车主线程结束"+Thread.currentThread().getName());
        return cartInfoExist;
    }

    /**
     * 查看购物车列表实现
     * @param
     * @return
     */
    @Override
    @Transactional
    public List<CartInfo> getCartList(String userId,String userTempId) {
        System.out.println("查看购物购物车主线程开启"+Thread.currentThread().getName());
        List<CartInfo> cartInfoList = new ArrayList<>();
        String cartKey = "";
        //登录
        if (!StringUtils.isEmpty(userId)){
            cartKey = this.getCartKey(userId);
            //如果登录的时候,临时id不为空,再检查临时用户的购物车有无,没有就无需合并
            if (!StringUtils.isEmpty(userTempId)){
                //既有用户id,还有临时用户id,再检查临时用户的购物车有无,没有就无需合并
                //获取临时用户的购物车
                String userTempIdCartKey = this.getCartKey(userTempId);
                Map<String,CartInfo> userTempIdMap = redisTemplate.opsForHash().entries(userTempIdCartKey);
                //判断临时用户的购物车是否存在
                if (!CollectionUtils.isEmpty(userTempIdMap)){
                    //临时用户购物车存在,需要合并
                    //获取用户的购物车
                    Map<String,CartInfo> userIdMap = redisTemplate.opsForHash().entries(cartKey);
                    //循环遍历临时用户的购物车,
                    // 用用户的购物车的map,containsKey.临时用户购物车的map的key
                    //如果为true说明存在相同商品,进行合并,false为不相同商品,添加
                    //普通Map循环
                    for (Map.Entry<String, CartInfo> tempIdEntry : userTempIdMap.entrySet()) {
                        //每次遍历将临时用户购物车的userId改为用户的userId
                        //将临时用户购物车的所有商品userid改为用户的userid
                        tempIdEntry.getValue().setUserId(userId);
                        //修改价格
                        BigDecimal skuPrice = productFeignClient.getSkuPrice(tempIdEntry.getValue().getSkuId());
                        if (userIdMap.containsKey(tempIdEntry.getKey())){
                            //有相同的商品id  合并
                            CartInfo cartInfoUser = userIdMap.get(tempIdEntry.getKey());
                            //两个购物车合并只需要修改商品数量,商品价格,设置为选中
                            cartInfoUser.setSkuPrice(skuPrice);
                            //修改数量
                            Integer tempIdSkuNum = tempIdEntry.getValue().getSkuNum();
                            cartInfoUser.setSkuNum(cartInfoUser.getSkuNum()+tempIdSkuNum);
                            //设置为选中
                            cartInfoUser.setIsChecked(1);
                        }else {
                            //没有相同的商品id  插入  , 设置为选中状态
                            tempIdEntry.getValue().setIsChecked(1);
                            tempIdEntry.getValue().setSkuPrice(skuPrice);
                            userIdMap.put(tempIdEntry.getKey(),tempIdEntry.getValue());
                        }
                    }


                    //将临时购物车按id排序,id为时间戳

                    //已经将临时用户购物车合并到用户购物车
                    //把最新的用户购物车放入redis,返回最新的购物项集合
                    redisTemplate.opsForHash().putAll(cartKey,userIdMap);
                    // cartInfoList = (List<CartInfo>) userIdMap.values(); ClassCastException:java.util.LinkedHashMap$LinkedValues cannot be cast to java.util.List
                    //使用ArrayList构造器完成LinkedHashMap转为ArrayList
                    cartInfoList = new ArrayList<>(userIdMap.values());
                    //合并完成以后将临时用户的购物车删除
                    redisTemplate.delete(userTempIdCartKey);

                    //异步合并数据库
                    cartInfoAsyncService.merge(userId,userTempId,cartInfoList);
                    System.out.println("查看购物购物车主线程结束"+Thread.currentThread().getName());
                    return cartInfoList;
                }

            }
        }
        //未登录
        if (!StringUtils.isEmpty(userTempId) && StringUtils.isEmpty(userId)){
            cartKey = this.getCartKey(userTempId);
        }
        cartInfoList = redisTemplate.opsForHash().values(cartKey);
        System.out.println("查看购物购物车主线程开启"+Thread.currentThread().getName());
        return cartInfoList;
    }

    /**
     * 删除购物项
     * @param skuId
     * @param userIdOrTempId
     */
    @Override
    public void deleteCart(Long skuId, String userIdOrTempId) {
        String cartKey = this.getCartKey(userIdOrTempId);
        //注意:skuId类型为long,在redis中为String,需要转换
        redisTemplate.opsForHash().delete(cartKey,skuId.toString());
        //异步删除DB
        cartInfoAsyncService.deleteByUserIdAndSkuId(userIdOrTempId,skuId);
    }

    /**
     * 更新选中状态
     * @param skuId
     * @param isChecked
     * @param userIdOrTempId
     */
    @Override
    public void checkCart(Long skuId, Integer isChecked, String userIdOrTempId) {
        String cartKey = this.getCartKey(userIdOrTempId);

        //选中状态 isChecked
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo!=null){
            cartInfo.setIsChecked(isChecked);
            redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);
        }
    }

    /**
     * 获取选中购物项集合
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        //获取id对应的redis的key
        String cartKey = this.getCartKey(userId.toString());
        //获取userId对应的checked为1的购物项
        //先获取该用户的所有购物项
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cartKey);
        //返回所有isCheck为1的
//        List<CartInfo> checkIdIs1CartList = cartInfoList.stream()
//                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
//                .collect(Collectors.toList());
        List<CartInfo> checkIdIs1CartList = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .peek(cartInfo -> cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId())))
                .collect(Collectors.toList());

        return checkIdIs1CartList;
    }

    /**
     * 设置购物车过期时间
     * @param cartKey
     */
    private void setCartKeyExpire(String cartKey) {
        this.redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     *
     获取购物车key
     */
    private String getCartKey(String userId) {
        // 用hashMap存购物车  key=user:id:cart  field=skuId value=购物项
        String cartKey = RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
        return cartKey;
    }


}
