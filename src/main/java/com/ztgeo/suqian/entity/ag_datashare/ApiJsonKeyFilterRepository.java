package com.ztgeo.suqian.entity.ag_datashare;

import org.springframework.data.repository.CrudRepository;

public interface ApiJsonKeyFilterRepository extends CrudRepository<ApiJsonKeyFilter, String> {
    ApiJsonKeyFilter findApiJsonKeyFiltersByApiIdEqualsAndFromUserEquals(String apiId, String fromUser);
}