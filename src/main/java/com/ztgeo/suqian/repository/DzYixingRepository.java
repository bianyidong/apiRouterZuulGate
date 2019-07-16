package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.DzYixing;
import org.springframework.data.repository.CrudRepository;

public interface DzYixingRepository extends CrudRepository<DzYixing, String> {

    // 请求，能过请求匹配URL并获取定制实例
    DzYixing findDzYixingsByUrlEquals(String url);

    // 响应，可以从ctx中获取api_id
    DzYixing findDzYixingsByApiIdEquals(String apiId);
}