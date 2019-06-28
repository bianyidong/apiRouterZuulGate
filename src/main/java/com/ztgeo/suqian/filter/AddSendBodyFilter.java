package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.repository.UserKeyInfoRepository;
import com.ztgeo.suqian.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;
import static com.ztgeo.suqian.filter.SafefromDataFilter.getObject;


/**
 * 用于鉴权
 */
@Component
public class AddSendBodyFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AddSendBodyFilter.class);
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.httpName}")
    private String httpName; // 存储用户发送数据的数据库名

    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入记录数据日志过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            String sendbody=ctx.get(GlobalConstants.SENDBODY).toString();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
            //1.获取heard中的userID和ApiID
            String apiID=request.getHeader("api_id");
            String userID=request.getHeader("form_user");
            List<ApiBaseInfo> list =apiBaseInfoRepository.findApiBaseInfosByApiIdEquals(apiID);
            ApiBaseInfo apiBaseInfo=list.get(0);

            //3.相关信息存入到mongodb中,有待完善日志
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            MongoDatabase mongoDB = mongoClient.getDatabase(httpName).withCodecRegistry(pojoCodecRegistry);
            MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);
            //封装参数
            HttpEntity httpEntity = new HttpEntity();
            String id= com.ztgeo.suqian.utils.StringUtils.getShortUUID();
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
            httpEntity.setSendBody(sendbody);
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
            ctx.set(GlobalConstants.RECORD_PRIMARY_KEY,id);
            ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return null;
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
        return 5;
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }


}
