package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiBaseInfoRepository extends CrudRepository<ApiBaseInfo,String> {

    int countApiBaseInfosByApiIdEquals(String api_id);
    List<ApiBaseInfo> findApiBaseInfosByApiIdEquals(String api_id);

//    @Query(value = "select * from dj_sjd left join dj_djb where qlrmc = ? and zjhm = ?",nativeQuery = true)
//    int queryPersonbdcinfobyzjhm(String idd,String sdjfs);
}
