package com.ztgeo.suqian.repository;


import com.ztgeo.suqian.entity.ag_datashare.ApiJgtoPtFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface ApiJgtoPtFilterRepository extends CrudRepository<ApiJgtoPtFilter,String> {

    @Query(nativeQuery = true,value = "SELECT count(*) from api_jgtopt_filter ajf LEFT JOIN api_user_filter auf on ajf.id=auf.api_id WHERE ajf.from_user=? and ajf.uri=? and auf.filter_bc=?")
    int countApiJgtoPtFilterByFromUserAndUriAndFilterBc(String fromUser, String Uri,String className);

    @Query(nativeQuery = true,value = "SELECT  ajf.* from api_jgtopt_filter ajf WHERE ajf.from_user=? and ajf.uri=?")
    ApiJgtoPtFilter queryApiJgtoPtFilterByFromUserAndUriAndFilterBc(String fromUser, String Uri);
}
