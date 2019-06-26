package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiUserFilter;
import org.springframework.data.repository.CrudRepository;

public interface ApiUserFilterRepository extends CrudRepository<ApiUserFilter,String> {

    int countApiUserFiltersByFilterBcEqualsAndApiIdEquals(String filterBC, String apiId);
}
