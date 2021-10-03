package com.yy.gmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
//@CrossOrigin
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

    /**
     * 品牌列表分页
     */
    @GetMapping("/baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable Long page,
                                @PathVariable Long limit){
        IPage<BaseTrademark> baseTrademarkPage = manageService.getBaseTrademarkPage(page,limit);

        return Result.ok(baseTrademarkPage);
    }

    /**
     * 根据category3Id获取spu分页列表
     */
    @GetMapping("/{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 Long category3Id){
        IPage<SpuInfo> spuInfoIPage = manageService.getSpuInfoPage(page,limit,category3Id);
        return Result.ok(spuInfoIPage);

    }

    /**
     * 获取销售属性
     */
    @GetMapping("/baseSaleAttrList")
    public Result getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    /**
     * 获取品牌列表
     */
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = manageService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }

    /**
     * 品牌类别添加
     */
    @PostMapping("/baseTrademark/save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        manageService.saveBaseTrademark(baseTrademark);
        return Result.ok();
    }

    /**
     * 根据分类3级id获取品牌类别
     * @param category3Id
     * @return
     */
    @GetMapping("/baseCategoryTrademark/findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> baseTrademarkList = manageService.findTrademarkListBycategory3Id(category3Id);
        return Result.ok(baseTrademarkList);
    }

    /**
     * 添加spu
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        manageService.saveSpuInfo(spuInfo);

        return Result.ok();
    }

    /**
     * 按spu_id查询图片
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result findSpuImageListBySpuId(@PathVariable Long spuId){
        List<SpuImage> spuImageList = manageService.findSpuImageListBySpuId(spuId);
        return Result.ok(spuImageList);
    }

    /**
     * 按spu_id查询spuAttrValue
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result findSpuSaleAttrListBySpuId(@PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrs = manageService.findSpuSaleAttrListBySpuId(spuId);
        return Result.ok(spuSaleAttrs);
    }

    /**
     * 保存sku
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * sku查询分页
     */
    @GetMapping("/list/{page}/{limit}")
    public Result findSkuInfoPage(@PathVariable Long page,
                                  @PathVariable Long limit){
        IPage<SkuInfo> skuInfoIPage = manageService.findSkuInfoPage(page,limit);
        return Result.ok(skuInfoIPage);
    }

    /**
     * 上架
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSaleBySkuId(@PathVariable Long skuId){
        manageService.onSaleBySkuId(skuId);
        return Result.ok();
    }


    /**
     * 下架
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSaleBySkuId(@PathVariable Long skuId){
        manageService.cancelSaleBySkuId(skuId);
        return Result.ok();
    }
}
