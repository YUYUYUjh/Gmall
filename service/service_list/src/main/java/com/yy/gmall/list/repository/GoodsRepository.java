package com.yy.gmall.list.repository;

import com.yy.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Yu
 * @create 2021-10-13 11:20
 */
@Repository
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
