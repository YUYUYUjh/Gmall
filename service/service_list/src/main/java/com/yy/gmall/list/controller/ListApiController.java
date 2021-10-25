package com.yy.gmall.list.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.list.service.ListService;
import com.yy.gmall.model.list.Goods;
import com.yy.gmall.model.list.SearchParam;
import com.yy.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Yu
 * @create 2021-10-12 20:02
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private ListService listService;

    @GetMapping("/inner/createIndex")
    public Result createIndex(){
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * 上架
     * @return
     */
    @GetMapping("/inner/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        listService.onSale(skuId);
        return Result.ok();
    }

    /**
     * 下架
     * @return
     */
    @GetMapping("/inner/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        listService.cancelSale(skuId);
        return Result.ok();
    }

    /**
     * 商品热度评分 (加分接口)
     */
    @PutMapping("/inner/incrHotScore/{skuId}/{score}")
    public void incrHotScore(@PathVariable Long skuId,@PathVariable Integer score){
        listService.incrHotScore(skuId,score);
    }

    /**
     * 商品搜索 :
     * 1.web-all 接收传递的参数 :searchParam
     * 2.searchParam 需要一个远程调用
     * 3.{searchParam有可能需要传递多个数据 ,多个数据需要JSON对象接收}
     */
    @PostMapping("/inner/searchData")
    public Result searchData(@RequestBody SearchParam searchParam){
        SearchResponseVo searchResponseVo = null;
        try {
            searchResponseVo = listService.search(searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.ok(searchResponseVo);
    }
}
