package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 响应过滤器
 *
 * @author bianyidong
 * @version 2019-7-19
 */
@Component
public class ResponseFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseFilter.class);
    private String api_id;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
//        String className = this.getClass().getSimpleName();
//        RequestContext ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
//        api_id = request.getHeader("api_id");
//        int count = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className, api_id);
//        if (count > 0) {
//            return true;
//        } else {
//            return false;
//        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        log.info("=================进入post通用过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getResponseDataStream();
            String rspBody = ctx.getResponseBody();
            //获取记录主键ID(来自routing过滤器保存的上下文)
            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "post通用过滤器访问者IP或记录ID未获取到");

//            String ContentType=ctx.getRequest().getContentType();
//            if ("text/xml".equals(ContentType)) {
//                log.info("请求为text/xml，返回日志不操作");
//                return null;
//            }
            if (!Objects.equals(null, inputStream)&&Objects.equals(null, rspBody)) {
                // 获取返回的body
                ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
                inputStreamOld = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 原始流
                //inputStreamNew = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 复制流
                inputStreamNew = inputStreamOld;
                // 获取返回的body字符串
                String responseBody = StreamUtils.copyToString(inputStreamOld, StandardCharsets.UTF_8);

                if (Objects.equals(null, responseBody)) {
                    throw new ZtgeoBizZuulException(CodeMsg.FAIL, "post通用过滤器响应报文未获取到");
                }
                ctx.setResponseBody(responseBody);
                log.info("post通用过滤器入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            } else if (!Objects.equals(null, rspBody)) {
                ctx.setResponseBody(rspBody);
                log.info("post通用过滤器入库完成(respBody is null)");
            } else {
                log.info("未接收到返回的任何数据,记录ID:{}", recordID);

            }
            ctx.set(GlobalConstants.RECORD_PRIMARY_KEY, recordID);
            ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z, "post通用过滤器异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s, CodeMsg.FAIL, "内部异常");
        } finally {
            ResponseSafeToSignFilter.getFindlly(inputStream, inputStreamOld, inputStreamNew);
        }
    }
}
