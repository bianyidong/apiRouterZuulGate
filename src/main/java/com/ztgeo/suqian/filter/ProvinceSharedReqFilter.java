package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.ApiNotionalSharedConfig;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.ApiNotionalSharedConfigRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.HttpOperation;
import com.ztgeo.suqian.utils.RSAUtils;
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
import java.util.concurrent.TimeUnit;

/**
 *  国土资源部接口---政务外网（省厅大数据中心转发的接口，需要TOKEN验证）
 */
@Component
public class ProvinceSharedReqFilter extends ZuulFilter {

    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiNotionalSharedConfigRepository apiNotionalSharedConfigRepository;
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;
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
        if(useCount == 0){
            return false;
        }else {
                return true;
        }
    }

    @Override
    public Object run() throws ZuulException {

        try {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = requestContext.getRequest();
            String api_id = httpServletRequest.getHeader("api_id");

            // 判断redis中是否有数据
            String redisKey = "token:" + api_id;
            String token = "Bearer " + getProviceToken(redisKey);

            requestContext.addZuulRequestHeader("Authorization",token);

            InputStream inReq = httpServletRequest.getInputStream();
            String requestBody = IOUtils.toString(inReq,Charset.forName("UTF-8"));


            // 重新配置请求体
            // 将JSON设置到请求体中，并设置请求方式为POST
            // BODY体设置
            final byte[] reqBodyBytes = requestBody.getBytes();
            requestContext.setRequest(new HttpServletRequestWrapper(httpServletRequest) {

                @Override
                public String getMethod() {
                    return "POST";
                }

                @Override
                public String getContentType() {
                    return "application/json;charset=utf-8";
                }

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
            throw new ZtgeoBizZuulException(e, CodeMsg.PROVICESHARED_ERROR, "转发国家共享接口异常");
        }


        return null;
    }

    // token获取与配置
    private synchronized String getProviceToken(String configKey) {
        boolean totalIsHasKey = redisTemplate.hasKey(configKey);

        // 不存在
        if (!totalIsHasKey) {
            String token = null;
            String tokenUrl = "https://2.211.38.98:8343/v1/apigw/oauth2/token";
            String tokenParam = "grant_type=client_credentials&client_id=8947f32223bf4174bc7a014a96666ffc&client_secret=2805d50ef71246d3a394e078ba4a68fc&scope=default";
            String tokenRespStr = HttpOperation.sendPostByApplicationXwwwFromUrlendcoded(tokenUrl,tokenParam);
            JSONObject tokenRespJson = JSONObject.parseObject(tokenRespStr);
            token = tokenRespJson.getString("access_token");

            redisTemplate.opsForValue().set(configKey, token);
            redisTemplate.expire(configKey, 3333, TimeUnit.SECONDS);

            return token;
        }else{
            // 存在
            String token = redisTemplate.opsForValue().get(configKey);
            return token;
        }
    }
}
