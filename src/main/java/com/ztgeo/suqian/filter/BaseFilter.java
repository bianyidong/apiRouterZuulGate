package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.context.annotation.FilterType;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Filter;

/**
 *  基本过滤器 BaseFilter
 *  用于过滤请求头中的API_ID
 *
 *
 *  注：宜兴定制过滤器在测试无问题后集成到此过滤器
 */
public class BaseFilter extends ZuulFilter {


    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -99;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        String api_id = httpServletRequest.getHeader("api_id");
        // 从数据库中判断是否存在api_id
        if(api_id != null){
            int count = apiBaseInfoRepository.countApiBaseInfosByApiIdEquals(api_id);
            if(count == 0){
                throw new ZtgeoBizZuulException(CodeMsg.API_FILTER_ERROR);
            }
        }else{
            throw new ZtgeoBizZuulException(CodeMsg.API_FILTER_ERROR);
        }
        return null;
    }
}
