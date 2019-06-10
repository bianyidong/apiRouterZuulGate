package com.ztgeo.suqian.zhangyu;

import com.alibaba.fastjson.JSONObject;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
public class TestController {

    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;


    @RequestMapping(value = "/test")
    public void test() {
        ApiBaseInfo apiBaseInfo = apiBaseInfoRepository.findById("1h5OiYUA").get();
        System.out.println(JSONObject.toJSON(apiBaseInfo));
    }
}
