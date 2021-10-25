package com.yy.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yy.gmall.model.list.SearchAttr;
import com.yy.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-09-28 18:36
 */
public interface ManageService {

    /**
     * 获取一级分类
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 获取二级分类
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 获取三级分类
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据id获取平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 添加属性值或者修改
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据属性id查询属性
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 品牌列表分页
     */
    IPage<BaseTrademark> getBaseTrademarkPage(Long page, Long limit);

    /**
     * 获取spu分页列表
     */
    IPage<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id);
    /**
     * 获取销售属性
     */
    List<BaseSaleAttr> getBaseSaleAttrList();
    /**
     * 获取品牌属性
     */
    List<BaseTrademark> getTrademarkList();
    /**
     * 品牌类别添加
     */
    void saveBaseTrademark(BaseTrademark baseTrademark);

    /**
     * 根据分类3级id获取品牌类别
     */
    List<BaseTrademark> findTrademarkListBycategory3Id(Long category3Id);

    /**
     * 添加spu
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 按spu_id查询图片
     */
    List<SpuImage> findSpuImageListBySpuId(Long spuId);

    /**
     * 按spu_id查询spuAttrValue
     */
    List<SpuSaleAttr> findSpuSaleAttrListBySpuId(Long spuId);

    /**
     * 保存sku
     */
    void saveSkuInfo(SkuInfo skuInfo);


    /**
     * sku查询分页
     */
    IPage<SkuInfo> findSkuInfoPage(Long page, Long limit);

    /**
     * 上架
     * @param skuId
     */
    void onSaleBySkuId(Long skuId);

    /**
     * 下架
     * @param skuId
     */
    void cancelSaleBySkuId(Long skuId);

    /**
     * 根据SkuId 来获取SkuInfo数据
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuId(Long skuId);

    /**
     * 获取skuInfo价钱
     * @param skuId
     * @return
     */
    BigDecimal getPriceBySkuId(Long skuId);

    /**
     * 根据category3Id查询三表关联视图
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryByCategory3Id(Long category3Id);

    /**
     * 获取销售属性和销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListBySkuIdAndSpuId(Long skuId,Long spuId);

    /**
     * 根据spuId  查询skuId  和 valueIds(将sale_attr_value_id拼接)
     * @param spuId
     * @return
     */
    Map getValueIdsAndSkuIdToMapBySpuId(Long spuId);

    /**
     * 根据skuId查询平台属性名称和平台属性值名称
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId);

    /**
     * 获取全部分类信息
     */
    List<JSONObject> getBaseCategoryList();

    /**
     * 首页商品分类
     * @return
     */
    List<Map> getCategoryList();

    /**
     * 根据品牌id获取品牌数据
     */
    BaseTrademark getTrademarkByTmId(Long tmId);

    /**
     * 根据skuId获取平台属性和平台属性值
     * 用于填充es数据
     */
    List<SearchAttr> getSearchAttrBySkuId(Long skuId);
}
