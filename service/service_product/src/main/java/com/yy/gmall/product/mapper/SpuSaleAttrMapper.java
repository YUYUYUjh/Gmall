package com.yy.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yy.gmall.model.product.SpuSaleAttr;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Yu
 * @create 2021-09-30 1:50
 */
@Repository
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    List<SpuSaleAttr> selecSpuSaleAttrListBySpuId(Long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListBySkuIdAndSpuId(@Param("skuId") Long skuId,@Param("spuId") Long spuId);
}
