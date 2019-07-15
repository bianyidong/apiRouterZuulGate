package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiChangeType;
import com.ztgeo.suqian.entity.ag_datashare.ApiIpWhitelistFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiChangeTypeRepository;
import com.ztgeo.suqian.repository.ApiIpWhitelistFilterRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import com.ztgeo.suqian.utils.XmlAndJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  宜兴地税定制---响应
 */
@Component
public class YXLTResp_DZ extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ApiChangeTypeRepository apiChangeTypeRepository;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    private String api_id;


    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return -88;
    }

    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getSimpleName();
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        api_id = httpServletRequest.getHeader("api_id");

        int count = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className,api_id);

        if(count == 0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            InputStream inputStream = ctx.getResponseDataStream();
            ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
            String responseBody = StreamUtils.copyToString(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), StandardCharsets.UTF_8);
            log.info("宜兴定制原接口响应体：" + responseBody);

            // utf8 -- gbk
            String xml = XmlAndJsonUtils.json2xml(responseBody);
            xml = xml.replaceAll("utf-8","GBK");
            xml = xml.replaceAll("UTF-8","GBK");

            // 增加jstl3BizPackage版本号
            xml = xml.replaceAll("<jslt3BizPackage>","<jslt3BizPackage version=\"1.0\">");
            log.info("宜兴定制转换XML后：" + xml);

            String apiId = ctx.getRequest().getHeader("api_id");
            ApiChangeType apiChangeType = apiChangeTypeRepository.findApiChangeTypesByApiIdEquals(apiId);
            String fromRespSample = apiChangeType.getFromRespSample();
            String realRespString = fromRespSample.replaceAll("###",xml);
            log.info("宜兴定制真实响应：" + realRespString);

            ctx.addZuulResponseHeader("Content-Type","text/xml");
            ctx.setResponseBody(realRespString);

        } catch (IOException e) {
            throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_RESP_ERROR);
        }
        return null;
    }

}
