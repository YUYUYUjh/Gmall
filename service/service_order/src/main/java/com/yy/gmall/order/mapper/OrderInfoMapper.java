package com.yy.gmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yy.gmall.model.order.OrderInfo;
import org.springframework.stereotype.Repository;

/**
 * @author Yu
 * @create 2021-10-20 19:00
 */
@Repository
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {


    IPage<OrderInfo> selectPageByUserId(Page<OrderInfo> pageParam, String userId);
}
