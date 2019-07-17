package com.ztgeo.suqian.filter.dz;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiChangeType;
import com.ztgeo.suqian.entity.ag_datashare.DzYixing;
import com.ztgeo.suqian.repository.DzYixingRepository;
import com.ztgeo.suqian.utils.XmlAndJsonUtils;
import io.micrometer.core.instrument.util.IOUtils;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiChangeTypeRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  宜兴地税定制---请求
 */
@Component
public class YXLTReqDZFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private DzYixingRepository dzYixingRepository;

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
        System.out.println(requestURI);
        DzYixing dzYixing = dzYixingRepository.findDzYixingsByUrlEquals(requestURI);
        if(StringUtils.isEmpty(dzYixing)){
            return false;
        }else{
            return true;
        }
    }
    private static Map<String, String> urlMap=new HashMap<>();
    static {
        urlMap.put("t", "/test/");
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
            if(StringUtils.isEmpty(currentContentType)) {
                if ("GET".equals(currentMethod)) {
                    reqXmlStr = httpServletRequest.getParameter("xml");
                } else {
                    throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_CONTENT_TYPE_METHOD_ERROR);
                }
            }else{
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
            ctx.set("api_id",dzYixing.getApiId());
            ctx.set("from_user",dzYixing.getFromUser());

            // 为写日志设置body体信息;
            ctx.set(GlobalConstants.SENDBODY, jsonReqStr.toJSONString());

            // 将JSON设置到请求体中，并设置请求方式为POST
            String newbody = jsonReqStr.toJSONString();
            // BODY体设置
            final byte[] reqBodyBytes = newbody.getBytes();
            ctx.setRequest(new HttpServletRequestWrapper(httpServletRequest){

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
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_REQ_ERROR);
        }
        return null;
    }
}
