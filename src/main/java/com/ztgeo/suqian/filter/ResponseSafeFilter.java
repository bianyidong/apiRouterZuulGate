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

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;
import static com.ztgeo.suqian.filter.SafeFilter.getSafeBool;

/**
 * 响应过滤器
 *
 * @author zoupeidong
 * @version 2018-12-7
 */
@Component
public class ResponseSafeFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseSafeFilter.class);
    @Autowired
    private RedisOperator redis;
    @Autowired
    private JdbcTemplate jdbcTemplate;
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
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return getSafeBool(jdbcTemplate);
    }



    @Override
    public Object run() throws ZuulException {
        log.info("=================进入post安全过滤器,接收返回的数据=====================");
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
            String Sign_secret_key = getjsonObject.getString("Sign_secret_key");
            String Sign_pub_key=getjsonObject.getString("Sign_pub_key");
            String Sign_pt_secret_key=getjsonObject.getString("Sign_pt_secret_key");
            String Sign_pt_pub_key=getjsonObject.getString("Sign_pt_pub_key");
            //获取接收方机构的密钥
            List<ApiBaseInfo> list = jdbcTemplate.query(" select * FROM api_base_info abi where abi.api_id ='" + apiID + "'",new BeanPropertyRowMapper<>(ApiBaseInfo.class));
            ApiBaseInfo apiBaseInfo=list.get(0);
            System.out.println(apiBaseInfo.getApi_owner_id());
            String apiUserID = redis.get(USER_REDIS_SESSION +":"+apiBaseInfo.getApi_owner_id());
            JSONObject apiUserIDJson  = JSONObject.parseObject(apiUserID);
            String Symmetric_pubkeyapiUserIDJson=apiUserIDJson.getString("Symmetric_pubkey");
            //String Sign_secret_keyapiUserIDJson = apiUserIDJson.getString("Sign_secret_key");
            String Sign_pub_keyapiUserIDJson=apiUserIDJson.getString("Sign_pub_key");

            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            MongoDatabase mongoDB = mongoClient.getDatabase(dbSafeName).withCodecRegistry(pojoCodecRegistry);
            MongoCollection<HttpEntity> collection = mongoDB.getCollection(userID + "_record", HttpEntity.class);

            String rspBody = ctx.getResponseBody();

            // 获取记录主键ID(来自routing过滤器保存的上下文)
            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL,"访问者IP或记录ID未获取到");
            if(!Objects.equals(null,inputStream)){
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
                String rspSignData=jsonresponseBody.get("sign").toString();
             log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                // 验证签名
                boolean rspVerifyResult = CryptographyOperation.signatureVerify(Sign_pub_keyapiUserIDJson, rspEncryptData, rspSignData);
                if (Objects.equals(rspVerifyResult, false))
                    throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
                // 解密
                String rspDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkeyapiUserIDJson, rspEncryptData);
                // 重新加密加签
                rspEncryptData = CryptographyOperation.aesEncrypt(Symmetric_pubkey, rspDecryptData);
                rspSignData = CryptographyOperation.generateSign(Sign_pt_secret_key, rspEncryptData);
                jsonresponseBody.put("data",rspEncryptData);
                jsonresponseBody.put("sign",rspSignData);
                String newbody=jsonresponseBody.toString();
                ctx.setResponseBody(newbody);
                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
                BasicDBObject newDoc = new BasicDBObject("$set",
                        new BasicDBObject().append("receiveBody", newbody));
                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                log.info("入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            }else if(!Objects.equals(null,rspBody)){
              log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                            JSONObject jsonObject = JSON.parseObject(rspBody);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
                // 验证签名
                boolean rspVerifyResult = CryptographyOperation.signatureVerify(Sign_pub_keyapiUserIDJson, data, sign);
                if (Objects.equals(rspVerifyResult, false))
                    throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
                // 解密
                String rspDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkeyapiUserIDJson, data);
                // 重新加密加签
                data = CryptographyOperation.aesEncrypt(Symmetric_pubkey, rspDecryptData);
                sign = CryptographyOperation.generateSign(Sign_pt_secret_key, data);
                //重新加载到response中
                jsonObject.put("data",data);
                jsonObject.put("sign",sign);
                String newbody=jsonObject.toString();
                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
                BasicDBObject newDoc = new BasicDBObject("$set",
                        new BasicDBObject().append("receiveBody", newbody));
                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                log.info("入库完成");
                ctx.setResponseBody(newbody);
            }else {
             log.info("未接收到{}返回的任何数据,记录ID:{}", accessClientIp,recordID);
                log.info("记录完成");
            }
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z,"post过滤器异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s,CodeMsg.FAIL, "内部异常");
        } finally {
            try {
                if (!Objects.equals(null, inputStream)) {
                    inputStream.close();
                }
                if (!Objects.equals(null, inputStreamOld)) {
                    inputStreamOld.close();
                }
                if (!Objects.equals(null, inputStreamNew)) {
                    inputStreamNew.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}