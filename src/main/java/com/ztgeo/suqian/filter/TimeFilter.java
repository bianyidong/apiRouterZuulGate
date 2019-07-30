package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiTimeFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiTimeFilterRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.TimeCheckUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 访问时间过滤器，判断请求是否在指定时间内访问
 * 1、未设置时间过滤器---默认全天访问，放行。
 * 2、已设置时间过滤器但未设置访问时间---默认全天访问，放行。
 * 3、已设置时间过滤器且已设置访问时间---规定时间内访问，非法时间内异常处理。
 */
@Component
public class TimeFilter extends ZuulFilter {

    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    @Resource
    private ApiTimeFilterRepository apiTimeFilterRepository;

    private String api_id = null;

    private boolean isConfig = false;

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

        int count = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className, api_id);

        if (count == 0) {
            return false;
        } else {
            isConfig = true;
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {

        boolean timeFlag = false;

        List<ApiTimeFilter> apiTimeFilterList = apiTimeFilterRepository.findApiTimeFiltersByApiIdEquals(api_id);

        //System.out.println(api_id + "\t" + apiTimeFilterList.size() + "\t" + isConfig);

        if (!isConfig) {
            //System.out.println("接口未配置访问时间过滤器！");
        } else {
            if (isConfig && apiTimeFilterList.size() == 0) {
                //System.out.println("接口已配置访问时间过滤器，但是没有配置具体时间段，默认全天可访问");
            } else {
                for (ApiTimeFilter apiTimeFilter : apiTimeFilterList) {
                    String stime = apiTimeFilter.getStime();
                    String etime = apiTimeFilter.getEtime();
                    timeFlag = TimeCheckUtils.hourMinuteBetween(stime, etime);
                }
                if (!timeFlag) {
                    throw new ZtgeoBizZuulException(CodeMsg.TIME_FILTER_ERROR);
                }
            }
        }
        return null;
    }
}
