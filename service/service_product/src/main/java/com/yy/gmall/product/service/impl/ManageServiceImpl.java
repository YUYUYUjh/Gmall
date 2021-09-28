package com.yy.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yy.gmall.common.execption.GmallException;
import com.yy.gmall.model.product.*;
import com.yy.gmall.product.mapper.*;
import com.yy.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.List;

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

}
