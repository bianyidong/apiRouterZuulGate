package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiRouterDefine;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiRouterDefineRepository extends CrudRepository<ApiRouterDefine,String> {

    List<ApiRouterDefine> findApiRouterDefinesByEnabledEquals(int enabled);
}
