package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiJsonKeyFilter;
import com.ztgeo.suqian.entity.ag_datashare.ApiJsonKeyFilterRepository;
import com.ztgeo.suqian.entity.ag_datashare.ApiUserFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Component
public class JSONFieldFilter extends ZuulFilter {

    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiJsonKeyFilterRepository apiJsonKeyFilterRepository;

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getSimpleName();
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        String api_id = httpServletRequest.getHeader("api_id");
        int count = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className, api_id);
        if (count == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        String apiId = httpServletRequest.getHeader("api_id");
        String fromUser = httpServletRequest.getHeader("from_user");

//        InputStream stream = requestContext.getResponseDataStream();
//        String body = IOUtils.toString(stream);
        String body = requestContext.getResponseBody();
        System.out.println(body);
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.containsKey("data") && jsonObject.containsKey("sign")) {
            body = jsonObject.get("data").toString();
        }

        // 获取过滤规则
        ApiJsonKeyFilter apiJsonKeyFilter = apiJsonKeyFilterRepository.findApiJsonKeyFiltersByApiIdEqualsAndFromUserEquals(apiId, fromUser);
        String param = apiJsonKeyFilter.getFieldList();

        JSONObject json = null;
        try {
            json = null;
            List<String> paramList = Arrays.asList(param.split(","));
            Configuration conf = Configuration.builder().build();
            DocumentContext context = null;
            for (String rule : paramList) {

                if (StringUtils.isEmpty(json)) {
                    context = JsonPath.using(conf).parse(body);
                    json = new JSONObject(context.read("$"));
                }

                context = JsonPath.using(conf).parse(json);
                context.delete(rule);
                json = new JSONObject(context.read("$"));
            }

        } catch (Exception e) {
            throw new ZtgeoBizZuulException(CodeMsg.JSON_KEY_VALUE_FILTER_ERROR);
        }
        if (jsonObject.containsKey("data") && jsonObject.containsKey("sign")) {
            jsonObject.put("data", json);
            RequestContext.getCurrentContext().setResponseBody(jsonObject.toJSONString());
        } else {
            RequestContext.getCurrentContext().setResponseBody(json.toJSONString());
        }
        return null;
    }
}
