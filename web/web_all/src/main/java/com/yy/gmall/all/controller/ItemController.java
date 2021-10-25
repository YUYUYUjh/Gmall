package com.yy.gmall.all.controller;

import com.yy.gmall.item.client.ItemFeignClient;
import com.yy.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-03 22:25
 */
@Controller
@RequestMapping
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @GetMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){

        Map map = itemFeignClient.getItemBySkuId(skuId);
        //Result<Map> ok = Result.ok(map);
        model.addAllAttributes(map);

        return "item/index";
    }

}
