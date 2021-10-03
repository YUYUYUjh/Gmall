package com.yy.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.yy.gmall.item.service.ItemService;
import com.yy.gmall.model.product.BaseAttrInfo;
import com.yy.gmall.model.product.BaseCategoryView;
import com.yy.gmall.model.product.SkuInfo;
import com.yy.gmall.model.product.SpuSaleAttr;
import com.yy.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yu
 * @create 2021-10-01 22:21
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 数据汇总
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {
        // 获取skuInfo信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
        // 获取商品实时价钱
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // 获取skuid对应的商品平台属性和平台属性值
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        // 获取1.2.3级分类信息
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // 查询所有sku的value的组合 , 点击切换
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        //获取销售属性+销售属性
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());

        //使用lambda表达式 ,
        List<HashMap<String, String>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("attrName", baseAttrInfo.getAttrName());
            map.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
            return map;
        }).collect(Collectors.toList());

        // 封装map返回
        HashMap<String,Object> map = new HashMap<>();
        map.put("skuInfo",skuInfo);
        map.put("price",skuPrice);
        map.put("attrList",skuAttrList);
        map.put("categoryView",categoryView);
        map.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        map.put("spuSaleAttrList",spuSaleAttrList);
        return map;
    }
}
