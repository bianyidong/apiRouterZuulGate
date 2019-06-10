package com.ztgeo.suqian.zhangyu.config;

import com.google.common.collect.Lists;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class LoadConfig {
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;

    public List<ApiBaseInfo> getApiBaseInfoList(){
        return  Lists.newArrayList(apiBaseInfoRepository.findAll());
    }
}
