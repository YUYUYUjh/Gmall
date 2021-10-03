package com.yy.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.model.product.*;
import com.yy.gmall.product.mapper.*;
import com.yy.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-09-28 18:37
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;


    /**
     * 获取一级分类
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        List<BaseCategory1> category1List = baseCategory1Mapper.selectList(null);
        return category1List;
    }

    /**
     * 获取二级分类
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("category1_id",category1Id);
        List<BaseCategory2> category2List = baseCategory2Mapper.selectList(wrapper);
        return category2List;
    }
    /**
     * 获取三级分类
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("category2_id",category2Id);
        List<BaseCategory3> category3List = baseCategory3Mapper.selectList(wrapper);
        return category3List;
    }


    /**
     * 根据id获取平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.selectAttrInfoList(category1Id,category2Id,category3Id);

        return attrInfoList;
    }
    /**
     * 添加属性值或者修改
     * @param baseAttrInfo
     */
    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断baseAttrInfo是否有id
        //有id更新
        if (baseAttrInfo.getId() != null){

            baseAttrInfoMapper.updateById(baseAttrInfo);

            //先把所有attid的删除,
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);
            //然后添加
            baseAttrInfo.getAttrValueList().forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }else{

            //无id添加
            baseAttrInfoMapper.insert(baseAttrInfo);
            if (baseAttrInfo.getAttrValueList()!=null){
                baseAttrInfo.getAttrValueList().forEach(baseAttrValue -> {
                    //将回填attrinfo id set 给 attrvalue
                    baseAttrValue.setAttrId(baseAttrInfo.getId());
                    baseAttrValueMapper.insert(baseAttrValue);
                });
            }
        }
    }
    /**
     * 根据属性id查询属性
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("attr_id",attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValueList;
    }
    /**
     * 品牌列表分页
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademarkPage(Long page, Long limit) {
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkMapper.selectPage(new Page<>(page,limit), null);
        return baseTrademarkIPage;
    }

    /**
     * 获取spu分页列表
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("category3_id",category3Id);
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(new Page<>(page, limit), wrapper);
        return spuInfoIPage;
    }
    /**
     * 获取销售属性
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);

    }
    /**
     * 获取品牌属性
     */
    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }
    /**
     * 品牌类别添加
     */
    @Override
    public void saveBaseTrademark(BaseTrademark baseTrademark) {
        baseTrademarkMapper.insert(baseTrademark);
    }
    /**
     * 根据分类3级id获取品牌类别
     */
    @Override
    public List<BaseTrademark> findTrademarkListBycategory3Id(Long category3Id) {
        //1.根据category3Id从BaseCategoryTrademark表获取对应trademarkId
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList  = baseCategoryTrademarkMapper.selectList(wrapper);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)){
            //将查询回来的数据的id们获取出来封装到一个集合
            List ids = new ArrayList();
            baseCategoryTrademarkList.stream().forEach(baseCategoryTrademark -> {
                ids.add(baseCategoryTrademark.getTrademarkId());
            });
            //2.拿着trademarkId去base_trademark查找对应数据
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectBatchIds(ids);
            return baseTrademarkList;
        }
        return null;
    }

    /**
     * 添加spu
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1.存储spuInfo表
        spuInfoMapper.insert(spuInfo);

        //2.存储图片表
        //2.1 获取所有图片 , 批量添加数据库
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            spuImageList.stream().forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            });
        }

        //3.存储销售属性表
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            spuSaleAttrList.stream().forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                //4.存储销售属性值表
                if (!CollectionUtils.isEmpty(spuSaleAttr.getSpuSaleAttrValueList())){
                    spuSaleAttr.getSpuSaleAttrValueList().stream().forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });

        }


    }

    /**
     * 按spu_id查询图片
     */
    @Override
    public List<SpuImage> findSpuImageListBySpuId(Long spuId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("spu_id",spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(wrapper);
        return spuImageList;
    }

    /**
     * 按spu_id查询spuAttrValue
     */
    @Override
    public List<SpuSaleAttr> findSpuSaleAttrListBySpuId(Long spuId) {
        /*QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("spu_id",spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(wrapper);

        if (!CollectionUtils.isEmpty(spuSaleAttrList)){

            spuSaleAttrList.stream().forEach(spuSaleAttr -> {
                QueryWrapper wrapper1 = new QueryWrapper();
                wrapper1.eq("spu_id",spuId);
                wrapper1.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.selectList(wrapper1);
                spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
            });
        }*/


        return  spuSaleAttrMapper.selecSpuSaleAttrListBySpuId(spuId);
    }

    /**
     * 保存sku
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1.存储到 skuInfo
        skuInfoMapper.insert(skuInfo);
        //2.存储到 SkuImage
        if (!CollectionUtils.isEmpty(skuInfo.getSkuImageList())) {
            skuInfo.getSkuImageList().stream().forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        //3.存储到 SkuAttrValue
        if (!CollectionUtils.isEmpty(skuInfo.getSkuAttrValueList())) {
            skuInfo.getSkuAttrValueList().stream().forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }
        //4.存储到 SkuSaleAttrValue
        if (!CollectionUtils.isEmpty(skuInfo.getSkuSaleAttrValueList())) {
            skuInfo.getSkuSaleAttrValueList().stream().forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
    }

    /**
     * sku查询分页
     */
    @Override
    public IPage<SkuInfo> findSkuInfoPage(Long page, Long limit) {
        return skuInfoMapper.selectPage(new Page<>(page,limit),null);
    }

    /**
     * 上架
     * @param skuId
     */
    @Override
    public void onSaleBySkuId(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(skuInfo.ONSALE);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 下架
     * @param skuId
     */
    @Override
    public void cancelSaleBySkuId(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(skuInfo.CANCELSALE);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 根据SkuId 来获取SkuInfo数据
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(Long skuId) {
        //1.获取skuInfo信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        //2.获取skuImage
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));

        //将image塞入skuinfo
        skuInfo.setSkuImageList(skuImageList);
        return null;
    }

    /**
     * 获取skuInfo价钱
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getPriceBySkuId(Long skuId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("id",skuId);
        wrapper.select("price");
        SkuInfo skuInfo = skuInfoMapper.selectOne(wrapper);
        return skuInfo.getPrice();
    }

    /**
     * 获取一二三级分类信息
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategoryByCategory3Id(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        return baseCategoryView;
    }

    /**
     *获取销售属性+销售属性值+锁定
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySkuIdAndSpuId(Long skuId, Long spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrListBySkuIdAndSpuId(skuId,spuId);
    }

    /**
     * 根据spuId  查询skuId  和 valueIds(将sale_attr_value_id拼接)
     * @param spuId
     * @return
     */
    @Override
    public Map getValueIdsAndSkuIdToMapBySpuId(Long spuId) {
        //声明map集合
        HashMap<Object, Object> hashMap = new HashMap<>();
        //获取skuId , 属性值Ids
        List<Map> mapList = skuSaleAttrValueMapper.selectValueIdsAndSkuIdToMapBySpuId(spuId);
        //赋值
        mapList.stream().forEach(map -> {
            hashMap.put(map.get("value_ids"),map.get("sku_id"));
        });
        return hashMap;
    }

    /**
     * 根据skuId查询平台属性名称和平台属性值名称
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId) {

        return baseAttrInfoMapper.selectBaseAttrInfoBySkuId(skuId);
    }


}
