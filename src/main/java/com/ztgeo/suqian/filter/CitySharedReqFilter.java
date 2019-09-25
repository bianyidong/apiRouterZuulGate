package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.isoftstone.sign.SignGeneration;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiCitySharedConfig;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.ApiCitySharedConfigRepository;
import com.ztgeo.suqian.repository.ApiNotionalSharedConfigRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.HttpOperation;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  国土资源部接口---政务外网（省厅大数据中心转发的接口，需要TOKEN验证）
 */
@Component
public class CitySharedReqFilter extends ZuulFilter {

    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiCitySharedConfigRepository apiCitySharedConfigRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getSimpleName();
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        String api_id = httpServletRequest.getHeader("api_id");

        int useCount = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className,api_id);
        int configCount = apiCitySharedConfigRepository.countApiCitySharedConfigsByApiIdEquals(api_id);
        if(useCount == 0){
            return false;
        }else {
            if(configCount == 0){
                return false;
            }else{
                return true;
            }
        }
    }

    @Override
    public Object run() throws ZuulException {

        try {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = requestContext.getRequest();
            String api_id = httpServletRequest.getHeader("api_id");

            // 获取配置信息
            ApiCitySharedConfig apiCitySharedConfig = apiCitySharedConfigRepository.findApiCitySharedConfigsByApiIdEquals(api_id);

            // 获取请求参数
            InputStream inReq = httpServletRequest.getInputStream();
            String requestBody = IOUtils.toString(inReq,Charset.forName("UTF-8"));

            // 配置公共部分
            String sk = apiCitySharedConfig.getSk();
            JSONObject requestBodyRealJson = new JSONObject();
            requestBodyRealJson.put("serviceId",apiCitySharedConfig.getServiceId());
            requestBodyRealJson.put("ak",apiCitySharedConfig.getAk());
            requestBodyRealJson.put("appId",apiCitySharedConfig.getAppId());
            requestBodyRealJson.put("timestamp",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            // 处理请求参数，循环KEY后写入
            Map<String,Object> requestBodyMap = (Map) JSON.parse(requestBody);
            for (Map.Entry<String, Object> entry : requestBodyMap.entrySet()) {
                requestBodyRealJson.put(entry.getKey(),entry.getValue());
            }

            // 请求加签处理，使用sk
            Map<String,Object> requestBodyRealMap = (Map) JSON.parse(requestBodyRealJson.toJSONString());
            String sign = SignGeneration.generationSign(requestBodyRealMap,sk);
            requestBodyRealJson.put("sign",sign);

            // 重新配置请求体
            // 将JSON设置到请求体中，并设置请求方式为POST
            // BODY体设置
            final byte[] reqBodyBytes = requestBodyRealJson.toJSONString().getBytes();
            requestContext.setRequest(new HttpServletRequestWrapper(httpServletRequest) {

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(reqBodyBytes);
                }

                @Override
                public int getContentLength() {
                    return reqBodyBytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return reqBodyBytes.length;
                }

            });
        } catch (Exception e) {
            throw new ZtgeoBizZuulException(e, CodeMsg.CITY_ERROR, "转发市级共享接口异常");
        }
        return null;
    }
}
