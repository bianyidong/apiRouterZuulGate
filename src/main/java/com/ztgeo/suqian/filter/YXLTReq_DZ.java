package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiChangeType;
import com.ztgeo.suqian.utils.XmlAndJsonUtils;
import io.micrometer.core.instrument.util.IOUtils;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiChangeTypeRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  宜兴地税定制---请求
 */
@Component
public class YXLTReq_DZ extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ApiChangeTypeRepository apiChangeTypeRepository;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    private String api_id;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -97;
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
        //请求方式转换（宜兴）
        try {
            RequestContext ctx = RequestContext.getCurrentContext();

            HttpServletRequest httpServletRequest = ctx.getRequest();
            String apiId = httpServletRequest.getHeader("api_id");

            String body = null;
            if (!ctx.isChunkedRequestBody()) {
                ServletInputStream inp = null;
                try {
                    inp = ctx.getRequest().getInputStream();

                    if (inp != null) {
                        body = IOUtils.toString(inp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            body = body.replaceAll("\r\n","");
            body = body.replaceAll("\t","");
            System.out.println("请求体：" + body);


            ApiChangeType apiChangeType = apiChangeTypeRepository.findApiChangeTypesByApiIdEquals(apiId);
            String fromReqSample = apiChangeType.getFromReqSample();
            String[] bodys = fromReqSample.split("###");
            String xmlStart = bodys[0].substring(bodys[0].lastIndexOf("<"));
            String xmlEnd = bodys[1].substring(bodys[1].indexOf("<"),bodys[1].indexOf(">") + 1);


            String regx_req =  xmlStart + "(.*?)" + xmlEnd;
            System.out.println(regx_req);
            Matcher req_matcher = Pattern.compile(regx_req).matcher(body);
            String req_xml = null;
            while (req_matcher.find()) {
                int i = 1;
                req_xml = req_matcher.group(i);
                i++;
            }

            System.out.println(req_xml);

            JSONObject jsonObject = XmlAndJsonUtils.xml2json(req_xml);
            byte[] reqBodyBytes = jsonObject.toString().getBytes("UTF-8");


            HttpServletRequest request = ctx.getRequest();

            ctx.addZuulRequestHeader("Content-Type", "application/json");
            ctx.setRequest(new HttpServletRequestWrapper(request) {
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
            throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_REQ_ERROR);
        }
        return null;
    }
}
