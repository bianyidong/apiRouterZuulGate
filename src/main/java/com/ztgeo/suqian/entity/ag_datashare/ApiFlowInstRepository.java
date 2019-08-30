package com.ztgeo.suqian.entity.ag_datashare;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiFlowInstRepository extends CrudRepository<ApiFlowInst, String> {

    int countApiFlowInstsByApiIdAndIpEquals(String apiId, String ip);

    List<ApiFlowInst> findApiFlowInstsByApiIdEqualsAndIpEqualsOrderByEndTimeDesc(String apiId, String ip);

    @Query(value = "select ifnull(sum(current_count),0) from api_flow_inst where api_id = ? and start_time between ? and ?",nativeQuery = true)
    int sumCurrentCountFromApiFlowInstsByStartTimeBetweenAnd(String apiId,long stime, long etime);
}