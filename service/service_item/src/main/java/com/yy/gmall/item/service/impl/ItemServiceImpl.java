package com.yy.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.yy.gmall.item.service.ItemService;
import com.yy.gmall.list.client.ListFeignClient;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Yu
 * @create 2021-10-01 22:21
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeignClient listFeignClient;
    /**
     * 数据汇总
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {

        Map<String,Object> map = new HashMap<>();

        //使用异步编排完成任务

        //获取skuInfo
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
            //保存skuInfo
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        // 获取商品实时价钱
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);
        }, threadPoolExecutor);

        //获取分类名称
        //需要依赖于获取skuInfo线程的结果
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            // 获取1.2.3级分类信息
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        }, threadPoolExecutor);

        // 获取skuid对应的商品平台属性和平台属性值
        CompletableFuture<Void> skuAttrListCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);

            List<HashMap<String, String>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
                HashMap<String, String> attrMap = new HashMap<>();
                attrMap.put("attrName", baseAttrInfo.getAttrName());
                attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());

            map.put("attrList", skuAttrList);
        }, threadPoolExecutor);

        // 查询所有sku的value的组合 , 点击切换
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            map.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));

        }, threadPoolExecutor);

        //获取销售属性+销售属性
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPoolExecutor);

        //访问商品时增加1热度
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId,1);
        }, threadPoolExecutor);


        CompletableFuture.allOf(skuInfoCompletableFuture,
                priceCompletableFuture,
                categoryViewCompletableFuture,
                skuAttrListCompletableFuture,
                valuesSkuJsonCompletableFuture,
                spuSaleAttrListCompletableFuture,
                incrHotScoreCompletableFuture).join();

        return map;
    }


}
