package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.ApiNotionalSharedConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiNotionalSharedConfigRepository extends CrudRepository<ApiNotionalSharedConfig,String> {

    int countApiNotionalSharedConfigsByUseridEquals(String userid);
}
