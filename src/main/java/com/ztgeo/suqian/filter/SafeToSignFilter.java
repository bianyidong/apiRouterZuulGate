package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;
import static com.ztgeo.suqian.filter.SafefromDataFilter.getObject;
import static com.ztgeo.suqian.filter.SafefromSignFilter.getSafeBool;


/**
 * 用于鉴权
 */
@Component
public class SafeToSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafeToSignFilter.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisOperator redis;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbSafeName}")
    private String dbSafeName; // 存储用户发送数据的数据库名

    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入安全密钥请求方解密验证过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");
            //2.获取body中的加密和加签数据并做解密验签
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到数据或签名");

            //获取redis中的key值
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            JSONObject getjsonObject = JSONObject.parseObject(str);
            String Sign_pt_secret_key=getjsonObject.getString("Sign_pt_secret_key");

            //重新加签
             String receiveSign = CryptographyOperation.generateSign(Sign_pt_secret_key, data);
            //重新加载到requset中
            jsonObject.put("sign",receiveSign);
            String newbody=jsonObject.toString();
            //System.out.println("newbody:"+newbody);
            //3.相关信息存入到mongodb中,有待完善日志
//            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
//                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
//            MongoDatabase mongoDB = mongoClient.getDatabase(dbSafeName).withCodecRegistry(pojoCodecRegistry);
//            MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);
//            //封装参数
//            HttpEntity httpEntity = new HttpEntity();
//            String id= com.ztgeo.suqian.utils.StringUtils.getShortUUID();
//            httpEntity.setID(id);
//            httpEntity.setSendUserID(userID);
//            httpEntity.setApiID(apiID);
//            httpEntity.setApiName(apiBaseInfo.getApi_name());
//            httpEntity.setApiPath(apiBaseInfo.getPath());
//            httpEntity.setReceiveUserID(apiBaseInfo.getApi_owner_id());
//            httpEntity.setReceiverUserName(apiBaseInfo.getApi_owner_name());
//            httpEntity.setContentType(request.getContentType());
//            httpEntity.setMethod(request.getMethod());
//            String accessClientIp = HttpUtils.getIpAdrress(request);
//            httpEntity.setSourceUrl(accessClientIp);
//            httpEntity.setSendBody(newbody);
//            LocalDateTime localTime = LocalDateTime.now();
//            httpEntity.setYear(localTime.getYear());
//            httpEntity.setMonth(localTime.getMonthValue());
//            httpEntity.setDay(localTime.getDayOfMonth());
//            httpEntity.setHour(localTime.getHour());
//            httpEntity.setMinute(localTime.getMinute());
//            httpEntity.setSecond(localTime.getSecond());
//            httpEntity.setCurrentTime(Instant.now().getEpochSecond());
//            // 封装body
//            collection.insertOne(httpEntity);
            //ctx.set(GlobalConstants.RECORD_PRIMARY_KEY, id);
            //ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return getObject(ctx, request, newbody);
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
        }
    }

    @Override
    public boolean shouldFilter() {
        return getSafeBool(jdbcTemplate,"SafeToSignFilter");
    }
    @Override
    public int filterOrder() {
        return 4;
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }


}
