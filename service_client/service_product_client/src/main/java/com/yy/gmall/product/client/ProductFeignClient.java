package com.yy.gmall.product.client;

import com.yy.gmall.model.list.SearchAttr;
import com.yy.gmall.model.product.*;
import com.yy.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-03 20:03
 */
@FeignClient(value = "service-product" , fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    /**
     * 获取skuInfo
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoBySkuId(@PathVariable Long skuId);

    /**
     * 获取价格
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId);

    /**
     * 根据三级分类id获取分类数据
     * @param category3Id
     * @return
     */
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    /**
     * 获取销售属性和销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId);

    /**
     * 获取销售数据值的组合
     * 根据spuId  查询skuId  和 valueIds(将sale_attr_value_id拼接)
     * @param spuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    /**
     * 获取skuid对应的商品销售属性和销售属性值
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);

    /**
     * 首页分类信息接口
     */
    @GetMapping("/api/product/inner/getCategoryList")
    public List<Map> getCategoryList();

    /**
     * 根据品牌id获取品牌数据
     * @param tmId
     * @return
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId")Long tmId);

    /**
     * 根据skuId获取平台属性和平台属性值
     * 用于填充es数据
     */
    @GetMapping("/api/product/inner/getSearchAttrBySkuId/{skuId}")
    public List<SearchAttr> getSearchAttrBySkuId(@PathVariable Long skuId);
}
