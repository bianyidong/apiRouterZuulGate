package com.ztgeo.suqian.filter;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
@Component
public class
RecordRoutingFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(RecordRoutingFilter.class);

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
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        InputStream inputStream = null;
        try {
            log.info("=================进入日志记录过滤器=====================");
            RequestContext ctx = RequestContext.getCurrentContext();
            Object routeHost = ctx.get("routeHost");
            Object requestURI = ctx.get(FilterConstants.REQUEST_URI_KEY);
            if (Objects.equals(null, routeHost) || Objects.equals(null, requestURI))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "未匹配到路由规则或请求路径获取失败");
            //1.从内存查找api信息
            //2. 判断api是否存在
            if (true) {
               //3.相关信息存入到mongodb中
                CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
                MongoDatabase mongoDB = mongoClient.getDatabase(dbNoticeName).withCodecRegistry(pojoCodecRegistry);
                //MongoCollection<NoticeEntity> sendCollection = mongoDB.getCollection(userLoginName + "_noticesend", NoticeEntity.class);
            } else {
                log.info("未匹配到注册路由,请求路径:{}", requestURI);
                throw new ZtgeoBizZuulException(CodeMsg.NOT_FOUND, "未匹配到注册路由,请求路径:" + routeHost + requestURI);
            }
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z, z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s, CodeMsg.FAIL, "routing过滤器内部异常");
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
