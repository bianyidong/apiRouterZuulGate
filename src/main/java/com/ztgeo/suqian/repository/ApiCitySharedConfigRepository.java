package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiCitySharedConfig;
import com.ztgeo.suqian.entity.ag_datashare.ApiNotionalSharedConfig;
import org.springframework.data.repository.CrudRepository;

public interface ApiCitySharedConfigRepository extends CrudRepository<ApiCitySharedConfig,String> {
    int countApiCitySharedConfigsByApiIdEquals(String apiId);

    ApiCitySharedConfig findApiCitySharedConfigsByApiIdEquals(String apiId);
}
