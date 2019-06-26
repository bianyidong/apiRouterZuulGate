package com.ztgeo.suqian.filter;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.HttpUtils;
import com.ztgeo.suqian.utils.StringUtils;
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
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Component
public class
GeneralFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(GeneralFilter.class);
    private String api_id;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbName}")
    private String dbName; // 存储用户发送数据的数据库名
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
        return -1;
    }

    /**
     * 返回一个boolean值来判断该过滤器是否要执行
     * @return
     */
    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getSimpleName();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        api_id=request.getHeader("api_id");
        int count = apiUserFilterRepository.countApiUserFiltersByFilterBcEqualsAndApiIdEquals(className,api_id);
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
            String routeHost = ctx.get("routeHost").toString();
            String requestURI = ctx.get(FilterConstants.REQUEST_URI_KEY).toString();
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");
            String apiID=request.getHeader("api_id");

            if (Objects.equals(null, routeHost) || Objects.equals(null, requestURI))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "未匹配到路由规则或请求路径获取失败");
            // 查找base_url和path
            List<ApiBaseInfo> list =apiBaseInfoRepository.findApiBaseInfosByBaseUrlEqualsAndPathEquals(routeHost,requestURI);
            //2. 判断api是否存在
            if (!Objects.equals(null, list) && list.size() != 0) {
               //3.相关信息存入到mongodb中,有待完善日志
                CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
                MongoDatabase mongoDB = mongoClient.getDatabase(dbName).withCodecRegistry(pojoCodecRegistry);
                MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);
                //封装参数
                HttpEntity httpEntity = new HttpEntity();
                ApiBaseInfo apiBaseInfo = list.get(0);
                String id=StringUtils.getShortUUID();
                httpEntity.setID(id);
                httpEntity.setSendUserID(userID);
                httpEntity.setApiID(apiID);
                httpEntity.setApiName(apiBaseInfo.getApiName());
                httpEntity.setApiPath(apiBaseInfo.getPath());
                httpEntity.setReceiveUserID(apiBaseInfo.getApiOwnerId());
                httpEntity.setReceiverUserName(apiBaseInfo.getApiOwnerName());
                httpEntity.setContentType(request.getContentType());
                httpEntity.setMethod(request.getMethod());
                String accessClientIp = HttpUtils.getIpAdrress(request);
                httpEntity.setSourceUrl(accessClientIp);
                httpEntity.setSendBody(body);
                LocalDateTime localTime = LocalDateTime.now();
                httpEntity.setYear(localTime.getYear());
                httpEntity.setMonth(localTime.getMonthValue());
                httpEntity.setDay(localTime.getDayOfMonth());
                httpEntity.setHour(localTime.getHour());
                httpEntity.setMinute(localTime.getMinute());
                httpEntity.setSecond(localTime.getSecond());
                httpEntity.setCurrentTime(Instant.now().getEpochSecond());
                // 封装body
                collection.insertOne(httpEntity);
                ctx.set(GlobalConstants.RECORD_PRIMARY_KEY, id);
                ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
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
