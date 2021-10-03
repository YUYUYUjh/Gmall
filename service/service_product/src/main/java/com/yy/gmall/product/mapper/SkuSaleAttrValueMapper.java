package com.yy.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yy.gmall.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.MapKey;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-01 2:11
 */
@Repository
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    @MapKey("value_ids")
    List<Map> selectValueIdsAndSkuIdToMapBySpuId(Long spuId);
}
