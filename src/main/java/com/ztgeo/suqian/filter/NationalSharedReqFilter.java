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
import com.ztgeo.suqian.utils.RSAUtils;
import io.micrometer.core.instrument.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

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

@Component
public class NationalSharedReqFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(NationalSharedReqFilter.class);
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

        ApiBaseInfo apiBaseInfo = apiBaseInfoRepository.queryApiBaseInfoByApiId(api_id);
        String apiOwnerid = apiBaseInfo.getApiOwnerId();

        int useCount = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className,api_id);
        int configCount = apiNotionalSharedConfigRepository.countApiNotionalSharedConfigsByUseridEquals(apiOwnerid);

        if(useCount == 0){
            return false;
        }else {
            if(configCount == 0){
                return false;
            }else {
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

            String userName = httpServletRequest.getHeader("userName");
            String requestType = httpServletRequest.getHeader("requestType");
            String businessNumber = httpServletRequest.getHeader("businessNumber");

            ApiBaseInfo apiBaseInfo = apiBaseInfoRepository.queryApiBaseInfoByApiId(api_id);
            String apiOwnerid = apiBaseInfo.getApiOwnerId();

            ApiNotionalSharedConfig apiNotionalSharedConfig = apiNotionalSharedConfigRepository.findById(apiOwnerid).get();
            log.info("获取国家配置信息：" + JSONObject.toJSONString(apiNotionalSharedConfig));

            String id = apiNotionalSharedConfig.getId();
            String token = apiNotionalSharedConfig.getToken();
            String deptName = apiNotionalSharedConfig.getDeptName();
            String qxdm = apiNotionalSharedConfig.getQxdm();

//            // 获取当前日期
//            String currentDays = new SimpleDateFormat("yyyyMMdd").format(new Date());
//            String configKey = currentDays + ":" + qxdm;
//            int xuHao = getXuHao(configKey);
//            String cxqqdh = currentDays + qxdm + String.format("%06d",xuHao);
            /**
             *  20191209  姜志伟要求修改
             */
            String cxqqdh = httpServletRequest.getHeader("cxqqdh");
            if(StringUtils.isEmpty(cxqqdh)){
                // 为空，非GF99参数，单独获取
                String currentDays = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String configKey = currentDays + ":" + qxdm;
                int xuHao = getXuHao(configKey);
                cxqqdh = currentDays + qxdm + String.format("%06d", xuHao);
            }

            InputStream inReq = httpServletRequest.getInputStream();
            String requestBody = IOUtils.toString(inReq,Charset.forName("UTF-8"));

            // 组织数据 待请求国家共享平台接口
            JSONObject contryReqJson = new JSONObject();

            // 配置请求头信息
            JSONObject contryHeadReqJson = new JSONObject();
            contryHeadReqJson.put("id",id);
            contryHeadReqJson.put("token",token);
            contryHeadReqJson.put("deptName",deptName);
            contryHeadReqJson.put("userName",userName);
            contryHeadReqJson.put("requestType",requestType);
            contryHeadReqJson.put("cxqqdh",cxqqdh);
            contryHeadReqJson.put("businessNumber",businessNumber);

            // 配置请求体
            String encodeRequestBody = RSAUtils.encodeByPublic(requestBody,token);

            // 配置请求参数
            contryReqJson.put("head",contryHeadReqJson);
            contryReqJson.put("param",encodeRequestBody);

            // 重新配置请求体
            // 将JSON设置到请求体中，并设置请求方式为POST
            String newbody = contryReqJson.toJSONString();
            // BODY体设置
            final byte[] reqBodyBytes = newbody.getBytes("UTF-8");
            requestContext.setRequest(new HttpServletRequestWrapper(httpServletRequest) {

                @Override
                public String getMethod() {
                    return "POST";
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
            log.info("转发国家级共享接口请求过滤器异常",e);
            throw new RuntimeException("30012-转发国家级共享接口请求过滤器异常");
        }
        return null;
    }
    // 序号获取与配置
    private synchronized int getXuHao(String configKey) {
        boolean totalIsHasKey = redisTemplate.hasKey(configKey);
        if (!totalIsHasKey) {
            redisTemplate.opsForValue().set(configKey, "1");
            redisTemplate.expire(configKey, 2, TimeUnit.DAYS);
            return 1;
        }else{
            int xuhao = Integer.valueOf(redisTemplate.opsForValue().get(configKey)) + 1;
            redisTemplate.opsForValue().set(configKey,String.valueOf(xuhao));
            return xuhao;
        }
    }
}
