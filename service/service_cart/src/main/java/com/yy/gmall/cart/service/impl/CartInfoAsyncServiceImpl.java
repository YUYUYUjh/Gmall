package com.yy.gmall.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.gmall.cart.mapper.CartInfoMapper;
import com.yy.gmall.cart.service.CartInfoAsyncService;
import com.yy.gmall.common.threadName.ThreadName;
import com.yy.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-18 16:17
 */
@Service
public class CartInfoAsyncServiceImpl extends ServiceImpl<CartInfoMapper,CartInfo> implements CartInfoAsyncService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Override
    @Async
    public void updateAsyncCart(CartInfo cartInfoExist) {
        System.out.println("修改----子线程开启" + Thread.currentThread().getName());
        cartInfoMapper.update(cartInfoExist,new QueryWrapper<CartInfo>()
                .eq("user_id",cartInfoExist.getUserId())
                .eq("sku_id",cartInfoExist.getSkuId()));
        System.out.println("修改----子线程结束" + Thread.currentThread().getName());
    }

    @ThreadName(methodName = "添加")
    @Override
    @Async
    public void insertAsyncCart(CartInfo cartInfoExist) {
        cartInfoMapper.insert(cartInfoExist);
    }

    /**
     * 合并购物车
     * @param userId
     * @param userTempId
     * @param cartInfoList
     */
    @ThreadName(methodName = "合并")
    @Override
    @Async
    @Transactional
    public void merge(String userId, String userTempId, List<CartInfo> cartInfoList) {

        //把合并的用户购物车和临时用户购物车全部删除
        QueryWrapper<CartInfo> wrapper = new QueryWrapper();
        wrapper.in("user_id",userId,userTempId);
        cartInfoMapper.delete(wrapper);
        //添加最新的购物车集合
        this.saveBatch(cartInfoList);

    }

    /**
     * 异步删除购物项
     * @param userIdOrTempId
     * @param skuId
     */
    @Override
    @ThreadName(methodName = "合并")
    public void deleteByUserIdAndSkuId(String userIdOrTempId, Long skuId) {
        //根据userIdAndSkuId删除
        cartInfoMapper.delete(new QueryWrapper<CartInfo>()
                .eq("user_id",userIdOrTempId)
                .eq("sku_id",skuId));
        //mysql高级 ,  类型转换会导致索引失效  所以传入的字段和数据库的字段要保持数据一致
    }
}
