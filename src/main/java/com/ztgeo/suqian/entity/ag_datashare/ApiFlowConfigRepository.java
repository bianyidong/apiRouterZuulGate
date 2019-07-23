package com.ztgeo.suqian.entity.ag_datashare;

import org.springframework.data.repository.CrudRepository;

public interface ApiFlowConfigRepository extends CrudRepository<ApiFlowConfig, String> {

    ApiFlowConfig findApiFlowConfigsByApiIdEquals(String apiId);
}