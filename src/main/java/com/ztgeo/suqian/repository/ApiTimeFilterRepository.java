package com.ztgeo.suqian.repository;

import com.ztgeo.suqian.entity.ag_datashare.ApiTimeFilter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiTimeFilterRepository extends CrudRepository<ApiTimeFilter,String> {

    List<ApiTimeFilter> findApiTimeFiltersByApiIdEquals(String api_id);
}
