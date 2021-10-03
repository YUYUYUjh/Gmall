package com.yy.gmall.product.api;

import com.yy.gmall.model.product.BaseAttrInfo;
import com.yy.gmall.model.product.BaseCategoryView;
import com.yy.gmall.model.product.SkuInfo;
import com.yy.gmall.model.product.SpuSaleAttr;
import com.yy.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-01 22:52
 */
@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    //定义一个数据接口 ,给service_item使用
    //inner : 代表这个是内部数据接口,给其他微服务使用

    /**
     * 获取skuInfo
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoBySkuId(@PathVariable Long skuId){
        SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
        return skuInfo;
    }

    /**
     * 获取价格
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        BigDecimal price = manageService.getPriceBySkuId(skuId);
        return price;
    }

    /**
     * 根据三级分类id获取分类数据
     * @param category3Id
     * @return
     */
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        return manageService.getCategoryByCategory3Id(category3Id);
    }

    /**
     * 获取销售属性和销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        return manageService.getSpuSaleAttrListBySkuIdAndSpuId(skuId,spuId);
    }

    /**
     * 获取销售数据值的组合
     * 根据spuId  查询skuId  和 valueIds(将sale_attr_value_id拼接)
     * @param spuId
     * @return
     */
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return manageService.getValueIdsAndSkuIdToMapBySpuId(spuId);
    }

    /**
     * 获取skuid对应的商品平台属性和平台属性值
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return manageService.getBaseAttrInfoBySkuId(skuId);
    }
}
