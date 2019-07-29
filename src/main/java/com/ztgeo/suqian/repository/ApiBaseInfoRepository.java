package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiBaseInfoRepository extends CrudRepository<ApiBaseInfo,String> {

    int countApiBaseInfosByApiIdEquals(String api_id);
    List<ApiBaseInfo> findApiBaseInfosByApiIdEquals(String api_id);

    @Query(value = "SELECT * FROM api_base_info where api_id=?",nativeQuery = true)
    ApiBaseInfo queryApiBaseInfoByApiId(String ApiId);
}
