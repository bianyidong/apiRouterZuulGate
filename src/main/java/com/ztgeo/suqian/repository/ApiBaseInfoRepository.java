package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import org.springframework.data.repository.CrudRepository;

public interface ApiBaseInfoRepository extends CrudRepository<ApiBaseInfo,String> {

    int countApiBaseInfosByApiIdEquals(String api_id);
}
