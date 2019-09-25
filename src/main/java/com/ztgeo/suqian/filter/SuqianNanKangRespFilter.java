package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSONObject;
import com.nankang.tool.EncrypterAESTool;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.ApiNotionalSharedConfig;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.ApiNotionalSharedConfigRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.RSAUtils;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 *  房地产平台接口---南康
 */
@Component
public class SuqianNanKangRespFilter extends ZuulFilter {

    @Value(value = "${nankangkey}")
    private String NanKangKey;

    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;
    @Resource
    private ApiNotionalSharedConfigRepository apiNotionalSharedConfigRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
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

        int useCount = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className, api_id);
        if (useCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {

        try {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = requestContext.getRequest();
            String api_id = httpServletRequest.getHeader("api_id");

            // 获取响应
            InputStream inputStream = requestContext.getResponseDataStream();
            ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
            String responseBody = StreamUtils.copyToString(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), StandardCharsets.UTF_8);

            // 响应转json
            JSONObject responseBodyJson = JSONObject.parseObject(responseBody);
            // 获取外层JSON
            JSONObject responseBodyJsonByNankang = responseBodyJson.getJSONObject("dataInfo");
            // 获取内层JSON
            String responseBodyStrByNankang = responseBodyJsonByNankang.getString("flatInfo");
            // 内层JSON解密并赋值
            String responseDataRealByNanKang = EncrypterAESTool.decryptByStr(responseBodyStrByNankang,NanKangKey);

            // 重新设置
            responseBodyJsonByNankang.put("flatInfo",responseDataRealByNanKang);
            responseBodyJson.put("dataInfo",responseBodyJsonByNankang);

            // 重新配置响应数据
            requestContext.setResponseBody(responseBodyJson.toJSONString());


        } catch (Exception e) {
            throw new ZtgeoBizZuulException(e, CodeMsg.NANKANG_ERROR, "转发南康接口异常");
        }
        return null;
    }
}
