package com.ztgeo.suqian.filter;

import javax.servlet.http.HttpServletRequest;

import com.ztgeo.suqian.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import java.util.Objects;
import java.util.Optional;

/**
 * 用于鉴权
 */
public class AccessAuthCheckFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AccessAuthCheckFilter.class);

//    @Autowired
//    private RedisTemplate redisTemplate;

    @Override
    public Object run() throws ZuulException {
//        try {
            log.info("=================进入pre过滤器,校验权限=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));

            // 检验是否在IP黑名单中
//            ListOperations<String,Object> listOperations = redisTemplate.opsForList();
//            List<Object> list = listOperations.range("ip-black-list",0,-1);
//            String realIp = HttpUtils.getIpAdrress(request);
//            boolean ipContains = list.contains(realIp);
//            if(!ipContains){
//                log.info("访问者IP:{}已被拉黑，拒绝访问", HttpUtils.getIpAdrress(request));
//                throw new ZtgeoBizZuulException(CodeMsg.BLACK_USER);
//            }
            return null;
//        } catch (ZuulException z) {
//            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
//        }
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public String filterType() {
        return "pre";
    }


}
