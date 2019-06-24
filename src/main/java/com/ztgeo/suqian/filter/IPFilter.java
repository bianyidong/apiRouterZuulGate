package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiIpWhitelistFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiIpWhitelistFilterRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  IP地址过滤器
 *  注：此过滤器为白名单过滤器，黑名单过滤器想法不成熟，待添加
 */
public class IPFilter extends ZuulFilter {

    @Resource
    private ApiIpWhitelistFilterRepository apiIpWhitelistFilterRepository;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;

    private String api_id;

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

        int count = apiUserFilterRepository.countApiUserFiltersByFilterBCEqualsAndApiIdEquals(className,api_id);

        if(count == 0){
            return false;
        }else{
            isConfig = true;
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {
        // 获取请求IP
        RequestContext ctx= RequestContext.getCurrentContext();
        HttpServletRequest req=ctx.getRequest();
        String current_ip = this.getIpAddr(req);

        // 判断逻辑
        boolean IPFlag = false;

        List<ApiIpWhitelistFilter> apiIpWhitelistFilterList = apiIpWhitelistFilterRepository.findApiIpWhitelistFiltersByApiIdEquals(api_id);

        if(!isConfig){
            System.out.println("接口未配置IP过滤器！");
        }else{
            if(isConfig && apiIpWhitelistFilterList.size() == 0){
                System.out.println("接口已配置IP过滤器，但是没有配置IP，默认全网IP可访问");
            }else{
                for ( ApiIpWhitelistFilter ipWhitelistFilter:apiIpWhitelistFilterList) {
                    if(current_ip.equals(ipWhitelistFilter.getIpContent())){
                        IPFlag = true;
                        break;
                    }
                }

                if(!IPFlag){
                    throw new ZtgeoBizZuulException(CodeMsg.IP_FILTER_ERROR);
                }
            }
        }
        return null;
    }

    /**
     * 获取Ip地址
     * @param request
     * @return
     */
    public  String getIpAddr(HttpServletRequest request){

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
