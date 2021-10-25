package com.yy.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.list.repository.GoodsRepository;
import com.yy.gmall.list.service.ListService;
import com.yy.gmall.model.list.*;
import com.yy.gmall.model.product.BaseCategoryView;
import com.yy.gmall.model.product.BaseTrademark;
import com.yy.gmall.model.product.SkuInfo;
import com.yy.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Yu
 * @create 2021-10-13 11:17
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 上架
     * @param skuId
     */
    @SneakyThrows
    @Override
    public void onSale(Long skuId) {

        //1.封装出一个Goods,把对应的数据库信息填充进es
        Goods goods = new Goods();

        //使用异步编排远程调用获取数据
        //远程调用获取skuInfo
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfoFuture = productFeignClient.getSkuInfoBySkuId(skuId);
            return skuInfoFuture;
        }, threadPoolExecutor);
        SkuInfo skuInfo = skuInfoCompletableFuture.get();

        //远程调用获取品牌数据
        CompletableFuture<BaseTrademark> baseTrademarkCompletableFuture = skuInfoCompletableFuture.thenApplyAsync(skuInfoFuture -> {
            BaseTrademark trademarkFuture = productFeignClient.getTrademark(skuInfo.getTmId());
            return trademarkFuture;
        }, threadPoolExecutor);
        BaseTrademark trademark = baseTrademarkCompletableFuture.get();

        //远程调用获取分类信息
        CompletableFuture<BaseCategoryView> baseCategoryViewCompletableFuture = skuInfoCompletableFuture.thenApplyAsync(skuInfoFuture -> {

            BaseCategoryView categoryViewFuture = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            return categoryViewFuture;
        }, threadPoolExecutor);
        BaseCategoryView categoryView = baseCategoryViewCompletableFuture.get();

        //远程调用获取平台属性id,name和平台属性值name
        CompletableFuture<List<SearchAttr>> listCompletableFuture = CompletableFuture.supplyAsync(() -> {
            List<SearchAttr> searchAttrListFuture = productFeignClient.getSearchAttrBySkuId(skuId);
            return searchAttrListFuture;
        }, threadPoolExecutor);
        List<SearchAttr> searchAttrList = listCompletableFuture.get();


//        // 商品Id skuId
//        @Id
//        private Long id;
        goods.setId(skuInfo.getId());
//        @Field(type = FieldType.Keyword, index = false)
//        private String defaultImg;
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
//        //  es 中能分词的字段，这个字段数据类型必须是 text！keyword 不分词！
//        @Field(type = FieldType.Text, analyzer = "ik_max_word")
//        private String title;
        goods.setTitle(skuInfo.getSkuName());
//        @Field(type = FieldType.Double)
//        private Double price;
        goods.setPrice(skuInfo.getPrice().doubleValue());
//        //  @Field(type = FieldType.Date)   6.8.1
//        @Field(type = FieldType.Date)
//        private Date createTime; // 新品
        Calendar calendar = Calendar.getInstance();
        //calendar.add(2,Calendar.HOUR);  给当前时间加两个小时
        goods.setCreateTime(calendar.getTime());
//        @Field(type = FieldType.Long)
//        private Long tmId;
        goods.setTmId(skuInfo.getTmId());
//        @Field(type = FieldType.Keyword)
//        private String tmName;
        goods.setTmName(trademark.getTmName());
//        @Field(type = FieldType.Keyword)
//        private String tmLogoUrl;
        goods.setTmLogoUrl(trademark.getLogoUrl());
//        @Field(type = FieldType.Long)
//        private Long category1Id;
        goods.setCategory1Id(categoryView.getCategory1Id());
//        @Field(type = FieldType.Keyword)
//        private String category1Name;
        goods.setCategory1Name(categoryView.getCategory1Name());
//        @Field(type = FieldType.Long)
//        private Long category2Id;
        goods.setCategory2Id(categoryView.getCategory2Id());
//        @Field(type = FieldType.Keyword)
//        private String category2Name;
        goods.setCategory2Name(categoryView.getCategory2Name());
//        @Field(type = FieldType.Long)
//        private Long category3Id;
        goods.setCategory3Id(categoryView.getCategory3Id());
//        @Field(type = FieldType.Keyword)
//        private String category3Name;
        goods.setCategory3Name(categoryView.getCategory3Name());
//        // 平台属性集合对象
//        // Nested 支持嵌套查询
//        @Field(type = FieldType.Nested)
//        private List<SearchAttr> attrs;
        goods.setAttrs(searchAttrList);
        this.goodsRepository.save(goods);
    }

    /**
     * 下架
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId, Integer score) {
        //1.先保存分数到redis中  积累到一定程度 统一更新es
        Double hotScore = redisTemplate.opsForZSet().incrementScore(RedisConst.HOTSCORE, skuId, score.doubleValue());
        //2.更新热度分数到es
        if (hotScore%10==0){
            Optional<Goods> byId = goodsRepository.findById(skuId);
            Goods goods = byId.get();
            if (goods!=null){
                goods.setId(skuId);
                goods.setHotScore(Math.round(hotScore));
                goodsRepository.save(goods);
            }
        }
    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品搜索
     * @param searchParam
     * @return
     * @throws IOException
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {

        //1.生产dsl语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);

        //2.执行dsl语句搜索
        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response);

        //3.对返回值结果对返回值SearchResponse进行结果解析 searchResponseVo --->> SearchResponseVo
        SearchResponseVo searchResponseVo = this.parseSearchResult(response);
        //trademarkList , attrList goodsList total在parseSearchResult方法中赋值

        //pageSieze,pageNo,totalPages在这个方法中赋值

        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //总页数 = (总记录数 + 每页显示条数-1)/每页显示条数
        //               14         5    -1    /  5
        long totalPages = (searchResponseVo.getTotal() + (searchParam.getPageSize())-1)/ searchParam.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        return searchResponseVo;

    }

    //设置返回结果集
    //searchResponse:获取到返回值的结果集
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse){
        //声明对象
        SearchResponseVo SearchResponseVo = new SearchResponseVo();
//        //品牌 此时vo对象中的id字段保留（不用写） name就是“品牌” value: [{id:100,name:华为,logo:xxx},{id:101,name:小米,log:yyy}]
//        private List<SearchResponseTmVo> trademarkList;
//        //所有商品的顶头显示的筛选属性
//        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
//        //检索出来的商品信息
//        private List<Goods> goodsList = new ArrayList<>();

        //获取到hits对象
        SearchHits hits = searchResponse.getHits();

        //获取subHits对象
        SearchHit[] subHits = hits.getHits();
        //循环遍历
        //定义一个goods集合
        if (subHits!=null && subHits.length>0){
            //            for (SearchHit subHit : subHits) {
            //                //获取到source
            //                String sourceAsString = subHit.getSourceAsString();
            //                //转换为goods
            //                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //                //判断是否有高亮显示
            //                if (subHit.getHighlightFields().get("title")!=null){
            //                    //有高亮就要获取到高亮中的名称
            //                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
            //                    //设置高亮名称
            //                    goods.setTitle(title.toString());
            //                }
            //                //将goods添加到集合
            //                goodsList.add(goods);
            //              }
            List<SearchHit> searchHits = Arrays.asList(subHits);
            List<Goods> goodsList = searchHits.stream().map((subHit) -> {
                //获取到source
                String sourceAsString = subHit.getSourceAsString();
                //转换为goods
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                //判断是否有高亮显示
                if (subHit.getHighlightFields().get("title") != null) {
                    //有高亮就要获取到高亮中的名称
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    //设置高亮名称
                    goods.setTitle(title.toString());
                }
                return goods;
            }).collect(Collectors.toList());
            //赋值 goodList  stream的赋值
            SearchResponseVo.setGoodsList(goodsList);
        }

        //获取品牌数据
        Aggregations aggregations = searchResponse.getAggregations();
        Map<String, Aggregation> aggregationMap = aggregations.asMap();
        //key = tmIdAgg | attrAgg  value = Aggregation
        //Aggregation的实现ParsedLongTerms中有获取buckets的方法
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        //List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        //通过获取桶中的数据,并赋值给searchResponseTmVo对象 并返回
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            //声明一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取桶中的tmId
            String tmId = bucket.getKeyAsString();
            //给品牌Id赋值
            searchResponseTmVo.setTmId(Long.parseLong(tmId));
            //获取品牌名称赋值到searchResponseTmVo
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //获取品牌logoUrl赋值到searchResponseTmVo
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            //返回品牌对象
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //获取平台属性数据  从聚合中获取 特殊性:nested
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrList = attrIdAgg.getBuckets().stream().map(bucket -> {
            //声明平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取平台属性id 平台属性名称 平台属性值名称
            //将id赋值
            Number attrId = bucket.getKeyAsNumber();
            searchResponseAttrVo.setAttrId(attrId.longValue());
            //attrName和attrValue在桶中
            //获取attrName的桶
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            // 获取桶中的数据
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            //将attrName赋值
            searchResponseAttrVo.setAttrName(attrName);
            //获取attrValue的桶
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            //获取桶中的数据
            List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
            //第一种:for循环
            //            List<String> valueNameList = new ArrayList<>();
            //            buckets.forEach(valueBucket -> {
            //                String valueName = valueBucket.getKeyAsString();
            //                valueNameList.add(valueName);
            //            });
            //第二种:使用stream流将获取回来的集合数据封装成list集合
            //            List<String> valueNameList = buckets.stream().map(valueBucket -> {
            //                String valueName = valueBucket.getKeyAsString();
            //                return valueName;
            //            }).collect(Collectors.toList());
            //第三种
            List<String> valueNameList = buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            //将attrValue赋值
            searchResponseAttrVo.setAttrValueList(valueNameList);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        //赋值attrsList
        SearchResponseVo.setAttrsList(attrList);

        //赋值trademarkList
        SearchResponseVo.setTrademarkList(trademarkList);
        //赋值 goodList  for循环的赋值
        //SearchResponseVo.setGoodsList(goodsList);

        //赋值total
        SearchResponseVo.setTotal(hits.getTotalHits());

        return SearchResponseVo;
    }

    //生成dsl语句 返回 searchRequest
    private SearchRequest buildQueryDsl(SearchParam searchParam){
        //首先声明一个查询器 {}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // {bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断是否根据分类id 进行检索
        if (searchParam.getCategory1Id()!=null){
            //{query --bool -- filter -- term}
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        if (searchParam.getCategory2Id()!=null){
            //{query --bool -- filter -- term}
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        if (searchParam.getCategory3Id()!=null){
            //{query --bool -- filter -- term}
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }

        //还有可能根据关键字检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()).operator(Operator.AND));
        }
        //根据品牌id 进行过滤
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            // 1:苹果
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                // {bool -- filter -- term -- tmId}
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }
        }
        //根据平台属性值过滤
        String[] props = searchParam.getProps();
        // 平台属性Id:平台属性值名称:平台属性名
        if (props!=null && props.length>0){
            //循环遍历
            for (String prop : props) {
                // prop = 平台属性Id : 平台属性值名称 : 平台属性名
                //对其进行分割
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    // 声明两个bool
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //内部的bool
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //赋值id
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    //赋值平台属性名
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //bool -- nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //最外层的bool -- filter
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }
        // {query --> bool}
        searchSourceBuilder.query(boolQueryBuilder);
        //排序:
        //获取排序规则
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            // order=2:desc
            //分割
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                String field = "";
                //判断是按照那种规则进行排序
                switch (split[0]){
                    case "1":
                        field  = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                    case "3":
                        field = "createTime";
                        break;
                }
                searchSourceBuilder.sort(field,"asc".equals(split[1]) ? SortOrder.ASC:SortOrder.DESC);
            }else {
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }

        //高亮:
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        highlightBuilder.preTags("<span style=color:red>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //分页:
        //from = 当前页-1 * 每页记录数
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        // 聚合:
        //品牌聚合 :
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                        .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //平台属性聚合:
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //创建SearchRequest 对象
        //GET /goods/_doc/17
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.source(searchSourceBuilder);

        //设置哪些field能够看到数据 , 哪些看不到数据
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        System.out.println("dsl:\t"+searchSourceBuilder.toString());
        return searchRequest;
    }
}
