package com.yy.gmall.item.service;

import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-01 22:19
 */
public interface ItemService {


    /**
     * 数据汇总
     * @param skuId
     * @return
     */
    Map<String,Object> getItem(Long skuId);

}
