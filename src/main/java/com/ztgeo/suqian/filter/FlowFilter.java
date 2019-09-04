package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.ApiFlowConfig;
import com.ztgeo.suqian.entity.ag_datashare.ApiFlowConfigRepository;
import com.ztgeo.suqian.entity.ag_datashare.ApiFlowInst;
import com.ztgeo.suqian.entity.ag_datashare.ApiFlowInstRepository;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *  限流过滤器
 *  暂时不支持定制过滤器
 */
@Component
public class FlowFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ApiFlowConfigRepository apiFlowConfigRepository;
    @Resource
    private ApiFlowInstRepository apiFlowInstRepository;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    private String api_id;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -55;
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
        try {
            long currentTimeMillis = System.currentTimeMillis();

            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = requestContext.getRequest();
            String apiId = httpServletRequest.getHeader("api_id");
            //String apiId = "1h5OiYUA";
            String ip = getRealIP(httpServletRequest);
            // 判断逻辑
            ApiFlowConfig apiFlowConfig = apiFlowConfigRepository.findApiFlowConfigsByApiIdEquals(apiId);

            // apiFlowConfig不为空，说明此API接口已经配置了限流
            if (!StringUtils.isEmpty(apiFlowConfig)) {

                String setime = getStartAndEndTime(currentTimeMillis);
                String dayStime = setime.split(",")[0];
                String dayEtime = setime.split(",")[1];

                int apiViewTotalCount = apiFlowInstRepository.sumCurrentCountFromApiFlowInstsByStartTimeBetweenAnd(apiId,Long.valueOf(dayStime),Long.valueOf(dayEtime));
                int limitTotalCount = apiFlowConfig.getLimitTotalCount();
                if(apiViewTotalCount >= limitTotalCount){
                    log.info("失败！接口已达最大访问量！");
                    // 设置zuul过滤当前请求，不对其进行路由
                    //requestContext.setSendZuulResponse(false);
                    // 设置返回码
                    //requestContext.setResponseStatusCode(401);
                    throw new ZtgeoBizZuulException(CodeMsg.FAILMAXSCOUNT,"限流访问异常");

                }


                String limitType = apiFlowConfig.getLimitType();
                long endTimeMillis = 0;
                if ("S".equals(limitType)) {
                    endTimeMillis = currentTimeMillis + 1000;
                } else if ("M".equals(limitType)) {
                    endTimeMillis = currentTimeMillis + 60000;
                } else if ("H".equals(limitType)) {
                    endTimeMillis = currentTimeMillis + 3600000;
                } else if ("D".equals(limitType)) {
                    endTimeMillis = currentTimeMillis + 86400000;
                }


                int viewCount = apiFlowInstRepository.countApiFlowInstsByApiIdAndIpEquals(apiId,ip);
                if(viewCount == 0){
                    apiFlowInstRepository.save(new ApiFlowInst(StreamOperateUtils.getShortUUID(), apiFlowConfig.getLimitObject(), apiId, ip, currentTimeMillis, endTimeMillis,1));

                    log.info("空值，成功！");
                }else{
                    ApiFlowInst apiFlowInst = apiFlowInstRepository.findApiFlowInstsByApiIdEqualsAndIpEqualsOrderByEndTimeDesc(apiId,ip).get(0);
                    long currentMaxEndTime = apiFlowInst.getEndTime();
                    int currentViewCount = apiFlowInst.getCurrentCount();
                    if(currentTimeMillis > currentMaxEndTime){
                        apiFlowInstRepository.save(new ApiFlowInst(StreamOperateUtils.getShortUUID(), apiFlowConfig.getLimitObject(), apiId, ip, currentTimeMillis, endTimeMillis,1));

                        log.info("超时，成功！");
                    }else{
                        int limitCount = apiFlowConfig.getLimitCount();
                        if(limitCount > currentViewCount){
                            apiFlowInst.setCurrentCount(apiFlowInst.getCurrentCount() + 1);
                            apiFlowInstRepository.save(apiFlowInst);

                            log.info("未超时且次数允许，成功！");
                        }else{

                            log.info("失败！请求过于频繁！");
                            // 设置zuul过滤当前请求，不对其进行路由
//                            requestContext.setSendZuulResponse(false);
//                            // 设置返回码
//                            requestContext.setResponseStatusCode(401);
                            throw new ZtgeoBizZuulException(CodeMsg.FAILQUILK,"限流访问异常");
                        }
                    }
                }
            }
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("异常啦！");
        }
        return null;
    }

    private String getRealIP(HttpServletRequest request) {

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

    private String getStartAndEndTime(long currentTimeMillis) throws ParseException {

        StringBuffer sb = new StringBuffer();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String YMD = simpleDateFormat.format(currentTimeMillis);
        String startTime = YMD + " 00:00:00";
        String endTime  = YMD + " 23:59:59";
        sb.append(simpleDateFormat1.parse(startTime).getTime()).append(",").append(simpleDateFormat1.parse(endTime).getTime());
        return sb.toString();
    }
}
