package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiIpWhitelistFilter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiIpWhitelistFilterRepository extends CrudRepository<ApiIpWhitelistFilter,String> {

    List<ApiIpWhitelistFilter> findApiIpWhitelistFiltersByApiIdEquals(String api_id);
}
