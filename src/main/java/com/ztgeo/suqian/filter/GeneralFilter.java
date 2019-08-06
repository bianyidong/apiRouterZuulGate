package com.ztgeo.suqian.filter;

import com.mongodb.MongoClient;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


@Component
public class
GeneralFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(GeneralFilter.class);
    private String api_id;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    /**
     * 过滤器的类型
     *
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }


    /**
     * 通过int值来定义过滤器的执行顺序，数值越小优先级越高。
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return -1;
    }

    /**
     * 返回一个boolean值来判断该过滤器是否要执行
     *
     * @return
     */
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
        InputStream inputStream = null;
        try {
            log.info("=================进入通用转发过滤器=====================");
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            ctx.set(GlobalConstants.SENDBODY, body);
            return null;
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s, CodeMsg.FAIL, "通用转发过滤器内部异常");
        } finally {
            try {
                if (!Objects.equals(null, inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
