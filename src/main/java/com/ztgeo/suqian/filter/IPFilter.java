package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiIpWhitelistFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiIpWhitelistFilterRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP地址过滤器
 * 注：此过滤器为白名单过滤器，黑名单过滤器想法不成熟，待添加
 */
@Component
public class IPFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        // 获取请求IP
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        String current_ip = this.getIpAddr(req);
        List<ApiIpWhitelistFilter> apiIpWhitelistFilterList = apiIpWhitelistFilterRepository.findApiIpWhitelistFiltersByApiIdEquals(api_id);
        if (!isConfig) {
            log.info("接口未配置IP过滤器！");
        } else {
            if (isConfig && apiIpWhitelistFilterList.size() == 0) {
                log.info("接口已配置IP过滤器，但是没有配置IP，默认全网IP可访问");
            } else {
                if (!judgeIPRule(apiIpWhitelistFilterList, current_ip)) {
                    throw new ZtgeoBizZuulException(CodeMsg.IP_FILTER_ERROR);
                }
            }
        }
        return null;
    }

    /**
     * 判断IP是否符合匹配规则
     */
    public boolean judgeIPRule(List<ApiIpWhitelistFilter> apiIpWhitelistFilterList, String current_ip) {
        boolean IPBoolean = false;

        String[] ipSplits = current_ip.split("\\.");

        for (ApiIpWhitelistFilter ipWhitelistFilter : apiIpWhitelistFilterList) {

            String ipConfig = ipWhitelistFilter.getIpContent();
            int startCount = ipConfig.length() - ipConfig.replace("*", "").length();
            StringBuffer sb = new StringBuffer();

            switch (startCount) {
                case 0:
                    sb.append(current_ip);
                    break;
                case 1:
                    sb.append(ipSplits[0]).append(".").append(ipSplits[1]).append(".").append(ipSplits[2]).append(".*");
                    break;
                case 2:
                    sb.append(ipSplits[0]).append(".").append(ipSplits[1]).append(".*.*");
                    break;
                case 3:
                    sb.append(ipSplits[0]).append(".*.*.*");
                    break;
                case 4:
                    sb.append("*.*.*.*");
                    break;
            }

            String ipAfter = sb.toString();

            if ("*.*.*.*".equals(ipAfter)) {
                IPBoolean = true;
            } else {
                Pattern ipPattern = Pattern.compile(ipConfig);
                Matcher m = ipPattern.matcher(ipAfter);
                IPBoolean = m.matches();
            }

            if (IPBoolean) {
                break;
            }
        }
        return IPBoolean;
    }

    /**
     * 获取Ip地址
     *
     * @param request
     * @return
     */
    public String getIpAddr(HttpServletRequest request) {

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
