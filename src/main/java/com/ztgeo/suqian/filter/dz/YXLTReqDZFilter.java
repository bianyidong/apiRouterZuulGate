package com.ztgeo.suqian.filter.dz;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.DzYixing;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.DzYixingRepository;
import com.ztgeo.suqian.utils.HttpOperation;
import com.ztgeo.suqian.utils.XmlAndJsonUtils;
import com.ztgeo.suqian.msg.CodeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.List;

/**
 *  宜兴地税定制---请求
 */
@Component
public class YXLTReqDZFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value(value = "${yxtokenpath}")
    private String YXTOKENREQPATH;

    @Resource
    private DzYixingRepository dzYixingRepository;
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -88;
    }

    @Override
    public boolean shouldFilter() {
        /**
         * 宜兴地税定制过滤器
         * 因为定制过滤器无APIID与FROMUSER，只能通过定制表中的是否有相同请求来判断是否执行过滤器。
         */
        // 获取当前请求
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = ctx.getRequest();
        // 获取请求方法名及对应的定制配置信息
        String requestURI = httpServletRequest.getRequestURI();
        DzYixing dzYixing = dzYixingRepository.findDzYixingsByUrlEquals(requestURI);
        if (StringUtils.isEmpty(dzYixing)) {
            return false;
        } else {
            return true;
        }
    }
    @Override
    public Object run() throws ZuulException {
        //请求方式转换（宜兴）
        try {
            // 获取当前请求
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = ctx.getRequest();

            // 获取请求方法名及对应的定制配置信息
            String requestURI = httpServletRequest.getRequestURI();
            DzYixing dzYixing = dzYixingRepository.findDzYixingsByUrlEquals(requestURI);

            // 判断content-type、method，获取请求参数
            String currentContentType = httpServletRequest.getHeader("Content-Type");
            String currentMethod = httpServletRequest.getMethod();

            String reqXmlStr = null;
            if (StringUtils.isEmpty(currentContentType)) {
                if ("GET".equals(currentMethod)) {
                    reqXmlStr = httpServletRequest.getParameter("xml");
                } else {
                    throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_CONTENT_TYPE_METHOD_ERROR);
                }
            } else {
                throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_CONTENT_TYPE_METHOD_ERROR);
            }


            log.info("请求参数：" + reqXmlStr);

            // 组织成XML，转换成JSON
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><root>").append(reqXmlStr).append("</root>");
            log.info("转换XML后：" + sb.toString());
            JSONObject jsonReqStr = XmlAndJsonUtils.xml2json(sb.toString());
            log.info("转换JSON后：" + jsonReqStr.toJSONString());

            // 增加头信息，因头信息会被过滤故设置到ctx中
            ctx.set("api_id", dzYixing.getApiId());
            ctx.set("from_user", dzYixing.getFromUser());

            // 宜兴接口访问需要ＴＯＫＥＮ
            String tokenReqJsonString = "{\"ClientId\":\"098f6bcd4621d373cade4e832627b4f6\",\"PlatformCode\":\"0\",\"UserName\":\"yxds\"}";
            String tokenJsonString = HttpOperation.sendJsonHttp(YXTOKENREQPATH,tokenReqJsonString);
            JSONObject tokenJson = JSONObject.parseObject(tokenJsonString);
            String status = tokenJson.getString("status");
            if("0".equals(status)){
                String token = tokenJson.getJSONObject("data").getString("accessToken");
                log.info("宜兴地税访问token:" + token);
                ctx.addZuulRequestHeader("Authorization",token);
            }else{
                throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_TOKEN_ERROR);
            }

            // 为写日志设置body体信息;
            ctx.set(GlobalConstants.SENDBODY, jsonReqStr.toJSONString());
            // 从数据库中获取定制URL真正的转发地址
            String apiId = dzYixing.getApiId();
            List<ApiBaseInfo> apiBaseInfoList = apiBaseInfoRepository.findApiBaseInfosByApiIdEquals(apiId);
            ApiBaseInfo apiBaseInfo = apiBaseInfoList.get(0);

            String realPath = apiBaseInfo.getPath();

            // 将JSON设置到请求体中，并设置请求方式为POST
            String newbody = jsonReqStr.toJSONString();
            // BODY体设置
            final byte[] reqBodyBytes = newbody.getBytes();
            ctx.setRequest(new HttpServletRequestWrapper(httpServletRequest) {

                @Override
                public String getMethod() {
                    return "POST";
                }

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(reqBodyBytes);
                }

                @Override
                public String getContentType() {
                    return "application/json";
                }

                @Override
                public int getContentLength() {
                    return reqBodyBytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return reqBodyBytes.length;
                }

                // 设置真正的转发地址
                @Override
                public String getRequestURI() {
                    return realPath;
                }
            });

        } catch (Exception e) {
            throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_REQ_ERROR);
        }
        return null;
    }
}
