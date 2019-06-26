package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;

/**
 * 响应过滤器
 *
 * @author bianyidong
 * @version 2019-6-21
 */
@Component
public class ResponseSafeAgainDataFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseSafeAgainDataFilter.class);
    private String api_id;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Autowired
    private RedisOperator redis;

    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbSafeName}")
    private String dbSafeName; // 存储用户发送数据的数据库名
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 2;
    }

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
        log.info("=================进入post返回安全重新加密过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getResponseDataStream();
            String userID=ctx.getRequest().getHeader("form_user");
            String apiID=ctx.getRequest().getHeader("api_id");
            //获取redis中userID的key值
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            JSONObject getjsonObject = JSONObject.parseObject(str);
            String Symmetric_pubkey=getjsonObject.getString("Symmetric_pubkey");
            String Sign_pt_secret_key=getjsonObject.getString("Sign_pt_secret_key");

//            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
//                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
//            MongoDatabase mongoDB = mongoClient.getDatabase(dbSafeName).withCodecRegistry(pojoCodecRegistry);
//            MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);
            String rspBody = ctx.getResponseBody();
            // 获取记录主键ID(来自routing过滤器保存的上下文)
//            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
//            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
//            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
//                throw new ZtgeoBizZuulException(CodeMsg.FAIL,"访问者IP或记录ID未获取到");
             if(!Objects.equals(null,rspBody)){
                //log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                JSONObject jsonObject = JSON.parseObject(rspBody);
                String data=jsonObject.get("data").toString();
                String sign=jsonObject.get("sign").toString();

                // 重新加密
                data = CryptographyOperation.aesEncrypt(Symmetric_pubkey, data);

                //重新加载到response中
                jsonObject.put("data",data);
                String newbody=jsonObject.toString();
//                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
//                BasicDBObject newDoc = new BasicDBObject("$set",
//                        new BasicDBObject().append("receiveBody", newbody));
//                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                log.info("入库完成");
                ctx.setResponseBody(newbody);
            }else if(!Objects.equals(null,inputStream)){
                // 获取返回的body
                ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
                inputStreamOld = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 原始流
                inputStreamNew = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 复制流
                // 获取返回的body字符串
                String responseBody = StreamUtils.copyToString(inputStreamOld, StandardCharsets.UTF_8);
                if (Objects.equals(null, responseBody)){
                    responseBody = "";
                    throw new ZtgeoBizZuulException(CodeMsg.FAIL,"响应报文未获取到");
                }
                JSONObject jsonresponseBody = JSON.parseObject(responseBody);
                String rspEncryptData=jsonresponseBody.get("data").toString();
             //log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                // 重新加密
                rspEncryptData = CryptographyOperation.aesEncrypt(Symmetric_pubkey, rspEncryptData);
                jsonresponseBody.put("data",rspEncryptData);
                String newbody=jsonresponseBody.toString();
                ctx.setResponseBody(newbody);
//                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
//                BasicDBObject newDoc = new BasicDBObject("$set",
//                        new BasicDBObject().append("receiveBody", newbody));
//                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                log.info("入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            }else {
             //log.info("未接收到{}返回的任何数据,记录ID:{}", accessClientIp,recordID);
                log.info("记录完成");
            }
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z,"post过滤器异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s,CodeMsg.FAIL, "内部异常");
        } finally {
            ResponseSafeToSignFilter.getFindlly(inputStream, inputStreamOld, inputStreamNew);
        }
    }
}
