package com.yy.gmall.product.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.model.product.*;
import com.yy.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yu
 * @create 2021-09-28 18:34
 */
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class ManageController {
    @Autowired
    private ManageService manageService;

    /**
     * 获取一级分类
     * @return
     */
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> category1List =  manageService.getCategory1();
        return Result.ok(category1List);
    }

    /**
     * 获取二级分类
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    /**
     * 获取三级分类
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    /**
     * 根据分类id获取平台属性
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(attrInfoList);
    }

    /**
     * 添加或修改属性值
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据属性id查询属性
     */
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        List<BaseAttrValue> attrValueList = manageService.getAttrValueList(attrId);
        return Result.ok(attrValueList);
    }



}
