package com.yy.gmall.item.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-01 22:40
 */
@RestController
@RequestMapping("/api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;
    //定义个远程调用的接口
    @GetMapping("{skuId}")
    public Map<String, Object> getItemBySkuId(@PathVariable Long skuId){
        Map<String, Object> map = itemService.getItem(skuId);
        return map;
    }
}
