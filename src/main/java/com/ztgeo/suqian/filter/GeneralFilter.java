package com.ztgeo.suqian.filter;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.msg.CodeMsg;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class
GeneralFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(GeneralFilter.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbNoticeName}")
    private String dbNoticeName; // 存储用户发送数据的数据库名
    /**
     * 过滤器的类型
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }



    /**
     * 通过int值来定义过滤器的执行顺序，数值越小优先级越高。
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 返回一个boolean值来判断该过滤器是否要执行
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String apiID=request.getHeader("api_id");
        int count = jdbcTemplate.queryForObject("SELECT COUNT(0) from api_user_filter  where api_id='" + apiID + "' and filter_bc='GeneralFilter'",Integer.class);
       if (count>0){
           return true;
       }else {
           return false;
       }

    }

    @Override
    public Object run() throws ZuulException {
        InputStream inputStream = null;
        try {
            log.info("=================进入通用转发过滤器=====================");
            //System.out.println(UUID.randomUUID().toString());
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            Object routeHost = ctx.get("routeHost");
            Object requestURI = ctx.get(FilterConstants.REQUEST_URI_KEY);
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");
            String apiID=request.getHeader("api_id");

            if (Objects.equals(null, routeHost) || Objects.equals(null, requestURI))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "未匹配到路由规则或请求路径获取失败");
            // 查找base_url和path
            List<ApiBaseInfo> list = jdbcTemplate.query("select * from api_base_info where base_url='" + routeHost + "' and path = '" + requestURI + "'", new BeanPropertyRowMapper<>(ApiBaseInfo.class));
            //2. 判断api是否存在
            if (!Objects.equals(null, list) && list.size() != 0) {
               //3.相关信息存入到mongodb中,有待完善日志
                CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
                MongoDatabase mongoDB = mongoClient.getDatabase(dbNoticeName).withCodecRegistry(pojoCodecRegistry);
                MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);
                //封装参数
                HttpEntity httpEntity = new HttpEntity();
                httpEntity.setMethod(request.getMethod());
                LocalDateTime localTime = LocalDateTime.now();
                httpEntity.setYear(localTime.getYear());
                httpEntity.setMonth(localTime.getMonthValue());
                httpEntity.setDay(localTime.getDayOfMonth());
                httpEntity.setHour(localTime.getHour());
                httpEntity.setMinute(localTime.getMinute());
                httpEntity.setSecond(localTime.getSecond());
                // 封装body
                collection.insertOne(httpEntity);
            } else {
                log.info("未匹配到注册路由,请求路径:{}", requestURI);
                throw new ZtgeoBizZuulException(CodeMsg.NOT_FOUND, "未匹配到注册路由,请求路径:" + routeHost + requestURI);
            }
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z, z.getMessage(), z.nStatusCode, z.errorCause);
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
