package com.yy.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.common.cache.GmallCache;
import com.yy.gmall.common.constant.MQConst;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.service.RabbitService;
import com.yy.gmall.model.list.SearchAttr;
import com.yy.gmall.model.product.*;
import com.yy.gmall.product.mapper.*;
import com.yy.gmall.product.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SearchAttrMapper searchAttrMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitService rabbitService;

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

        // 商品上架之后, 将上架商品添加到索引库
        //三个参数 1:交换机  2.RoutingKey  3.消费者需要什么传递什么
        this.rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_GOODS,
                MQConst.ROUTING_GOODS_UPPER,
                skuId);
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
        //商品下架时从索引库删除
        //三个参数 1:交换机  2.RoutingKey  3.消费者需要什么传递什么
        this.rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_GOODS,
                MQConst.ROUTING_GOODS_LOWER,
                skuId);
    }

    /**
     * 根据SkuId 来获取SkuInfo数据
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "sku:")
    public SkuInfo getSkuInfoBySkuId(Long skuId) {
        //return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        //获取skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo!=null){
            List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
            skuInfo.setSkuImageList(skuImageList);

        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        //定义缓存的key sku:skuId:info
        String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        //定义锁的key
        String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
        //1.先从缓存中获取数据
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
        //判断缓存是否有数据
        if(skuInfo==null){
            //没有
            Config config = new Config();
            config.setLockWatchdogTimeout(1000L);
            config.useSingleServer().setAddress("redis://192.168.6.111:6379");
            redissonClient = Redisson.create(config);
            RLock lock = redissonClient.getLock(lockKey);
            //2.获取上锁对象
            //RLock lock = redissonCLient.getLock(lockKey);
            //3.上锁
            try {
                //参数1:尝试获取锁的时间 参数2:设置获取到锁的过期时间
                boolean res = lock.tryLock(1, 3, TimeUnit.SECONDS);
                if (res) {
                    //获取到锁
                    //4.查询DB
                    skuInfo = skuInfoMapper.selectById(skuId);
                    //5.防止缓存穿透
                    if (null == skuInfo){
                        skuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey,skuInfo,5,TimeUnit.MINUTES);

                    }else{
                        //查询图片
                        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                        //将image塞入skuinfo
                        skuInfo.setSkuImageList(skuImageList);
                        //缓存一天  防止缓存雪崩 过期时间加上一个随机时间
                        Random random = new Random();
                        int i = random.nextInt(10);
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT+i,TimeUnit.SECONDS);
                    }
                }else {
                    //没获取到锁
                    Thread.sleep(1000);
                    return getSkuInfoBySkuId(skuId);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                //手动解锁
                lock.unlock();
            }
        }
        //缓存不为空直接返回
        return skuInfo;
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
        if (skuInfo!= null){
            return skuInfo.getPrice();
        }
        return new BigDecimal(999999.9999);
    }

    /**
     * 获取一二三级分类信息
     * @param category3Id
     * @return
     */
    @Override
    @GmallCache(prefix = "CategoryByCategory3Id:")
    public BaseCategoryView getCategoryByCategory3Id(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        if (baseCategoryView==null){
            return new BaseCategoryView();
        }
        return baseCategoryView;
    }

    /**
     *获取销售属性+销售属性值+锁定
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "SpuSaleAttrListBySkuIdAndSpuId:")
    public List<SpuSaleAttr> getSpuSaleAttrListBySkuIdAndSpuId(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListBySkuIdAndSpuId(skuId, spuId);
        if (spuSaleAttrs == null){
            List<SpuSaleAttr> spuSaleAttrList = new ArrayList<>();
            return spuSaleAttrList;
        }

        return spuSaleAttrs;
    }

    /**
     * 根据spuId  查询skuId  和 valueIds(将sale_attr_value_id拼接)
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "ValueIdsAndSkuIdToMapBySpuId:")
    public Map getValueIdsAndSkuIdToMapBySpuId(Long spuId) {
        //声明map集合
        HashMap<Object, Object> hashMap = new HashMap<>();
        //获取skuId , 属性值Ids
        List<Map> mapList = skuSaleAttrValueMapper.selectValueIdsAndSkuIdToMapBySpuId(spuId);
        //赋值
        if (!CollectionUtils.isEmpty(mapList)){

            mapList.stream().forEach(map -> {
                hashMap.put(map.get("value_ids"),map.get("sku_id"));
            });

        }
        return hashMap;
    }

    /**
     * 根据skuId查询平台属性名称和平台属性值名称
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "BaseAttrInfoBySkuId:")
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId) {
        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.selectBaseAttrInfoBySkuId(skuId);
        if (attrInfoList == null){
            return new ArrayList<BaseAttrInfo>();
        }
        return attrInfoList;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        return null;
    }

    @Override
    @GmallCache(prefix = "indexCategoryList")
    public List<Map> getCategoryList() {
        //获取所有三个等级分类的数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        List<Map> list = new ArrayList<>();
        //将所有数据按categroup1Id分类 , 得到map集合 , key是category1Id,value是category1Id对应的数据集合
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 1;
        //循环按1id分组的数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category1Map.entrySet()) {
            Long category1Id = entry1.getKey();
            List<BaseCategoryView> category1List = entry1.getValue();
            Map map1 = new HashMap();
            map1.put("index",index);
            map1.put("categoryName",category1List.get(0).getCategory1Name());
            map1.put("categoryId",category1Id);

            index++;

            List<Map> list2 = new ArrayList<>();
            //将按1id分组的数据再按2id分组 得到key是category2Id,value是category2Id对应的数据集合
            Map<Long, List<BaseCategoryView>> categoryMap2 = category1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : categoryMap2.entrySet()) {
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> category2List = entry2.getValue();
                Map map2 = new HashMap();
                map2.put("categoryName",category2List.get(0).getCategory2Name());
                map2.put("categoryId",category2Id);

                List<Map> list3 = new ArrayList<>();
                for (BaseCategoryView baseCategory3 : category2List) {
                    Map map3 = new HashMap();
                    map3.put("categoryName",baseCategory3.getCategory3Name());
                    map3.put("categoryId",baseCategory3.getCategory3Id());
                    list3.add(map3);
                }

                map2.put("categoryChild",list3);
                list2.add(map2);
            }

            map1.put("categoryChild",list2);
            list.add(map1);
        }
        return list;
    }

    /**
     * 根据品牌id获取品牌数据
     * @param tmId
     * @return
     */
    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    /**
     * 根据skuId获取平台属性和平台属性值
     * 用于填充es数据
     */
    @Override
    public List<SearchAttr> getSearchAttrBySkuId(Long skuId) {
        List<SearchAttr> searchAttrList = searchAttrMapper.selectList(new QueryWrapper<SearchAttr>().eq("sku_id", skuId));
        return searchAttrList;
    }
}
