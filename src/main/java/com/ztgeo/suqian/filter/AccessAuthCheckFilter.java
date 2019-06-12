package com.ztgeo.suqian.filter;

import javax.servlet.http.HttpServletRequest;

import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 用于鉴权
 */
@Component
public class AccessAuthCheckFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AccessAuthCheckFilter.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入pre过滤器,校验权限=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
//            InputStream in = ctx.getRequest().getInputStream();
//            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
//            System.out.println("body:" + body);
//            JSONObject jsonObject = JSON.parseObject(body);
//            System.out.println(jsonObject.get("token"));
//            jsonObject.get("token").toString();
//            JSONObject jsonObject1 = JSON.parseObject(jsonObject.get("token").toString());
//            System.out.println(jsonObject1.get("userID"));
//            String userId=jsonObject1.get("userID").toString();
//            String apiId=jsonObject1.get("apiID").toString();
//           int cout=  jdbcTemplate.queryForObject("select COUNT(0) from api_user_rel t where t.api_id='"+apiId+"' and  t.user_id='"+userId+"'",Integer.class);
//           if(cout<=0){
//               log.info("访问者:{}无权限访问接口，请开通权限",userId);
//               throw new ZtgeoBizZuulException(CodeMsg.NOT_FOUNDUSER);
//           }

            // 检验是否在IP黑名单中
            //ListOperations<String,Object> listOperations = redisTemplate.opsForList();
//            List<Object> list = listOperations.range("ip-black-list",0,-1);
//            String realIp = HttpUtils.getIpAdrress(request);
//            boolean ipContains = list.contains(realIp);
//            if(ipContains){
//                log.info("访问者IP:{}已被拉黑，拒绝访问", HttpUtils.getIpAdrress(request));
//                throw new ZtgeoBizZuulException(CodeMsg.BLACK_USER);
//            }
            return null;
       // }
//        catch (ZuulException z) {
//            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
        }
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
