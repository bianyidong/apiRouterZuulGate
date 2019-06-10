package com.ztgeo.suqian.zhangyu.filter.base;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.zhangyu.config.LoadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * IP过滤器---测试
 * 动态过滤：
 * 1、使用shouldFilter方法进行过滤，比方法中的判断条件来自redis或是数据库中的配置信息
 */
@Component
public class IPFilter  extends ZuulFilter {
    private Logger logger =  LoggerFactory.getLogger(getClass());

    @Resource
    private LoadConfig loadConfig;

    @Override
    public String filterType() {
        System.out.println("filterType");
        return "pre";
    }

    @Override
    public int filterOrder() {
        System.out.println("filterOrder");
        return -100;
    }

    @Override
    public boolean shouldFilter() {
        System.out.println("shouldFilter");
        String ip = "0:0:0:0:0:0:0:";
        if("0:0:0:0:0:0:0:1".equals(ip)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("run");
        RequestContext ctx= RequestContext.getCurrentContext();
        HttpServletRequest req=ctx.getRequest();
        String ipAddr=this.getIpAddr(req);
        logger.info("请求IP地址为：[{}]",ipAddr);
//        //配置本地IP白名单，生产环境可放入数据库或者redis中
//        List<String> ips=new ArrayList<String>();
//        ips.add("172.0.0.1");
//
//        if(!ips.contains(ipAddr)){
//            logger.info("IP地址校验不通过！！！");
//            ctx.setResponseStatusCode(401);
//            ctx.setSendZuulResponse(false);
//            ctx.setResponseBody("IpAddr is forbidden!");
//        }
//        logger.info("IP校验通过。");
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
