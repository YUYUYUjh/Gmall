package com.yy.gmall.product.service;

import com.yy.gmall.model.product.*;

import java.util.List;

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


}
