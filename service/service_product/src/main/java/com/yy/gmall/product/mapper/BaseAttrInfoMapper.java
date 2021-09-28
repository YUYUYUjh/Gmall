package com.yy.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yy.gmall.model.product.BaseAttrInfo;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Yu
 * @create 2021-09-28 19:56
 */
@Repository
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {


    List<BaseAttrInfo> selectAttrInfoList(@Param("category1Id") Long category1Id,
                                          @Param("category2Id") Long category2Id,
                                          @Param("category3Id") Long category3Id);
}
