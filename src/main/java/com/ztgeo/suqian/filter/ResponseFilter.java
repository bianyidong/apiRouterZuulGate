package com.ztgeo.suqian.filter;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import com.ztgeo.suqian.utils.StringUtils;
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
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


/**
 * 响应过滤器
 *
 * @author bianyidong
 * @version 2019-6-21
 */
@Component
public class ResponseFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseFilter.class);
    private String api_id;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbName}")
    private String dbName; // 存储用户发送数据的数据库名

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1;
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
        log.info("=================进入post通用过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            String userID = ctx.getRequest().getHeader("form_user");
            inputStream = ctx.getResponseDataStream();
            String rspBody = ctx.getResponseBody();
            //获取记录主键ID(来自routing过滤器保存的上下文)
            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "访问者IP或记录ID未获取到");
            if (!Objects.equals(null, inputStream)) {
                System.out.println(inputStream.available());
                // 获取返回的body
                ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
                inputStreamOld = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 原始流
                //inputStreamNew = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 复制流
                inputStreamNew = inputStreamOld;
                // 获取返回的body字符串
                String responseBody = StreamUtils.copyToString(inputStreamOld, StandardCharsets.UTF_8);
                boolean a=StringUtils.isBlank(responseBody);
//                if (StringUtils.isBlank(responseBody)) {
//                    throw new ZtgeoBizZuulException(CodeMsg.FAIL, "响应报文未获取到");
//                }
                if (Objects.equals(null, responseBody)) {
                    throw new ZtgeoBizZuulException(CodeMsg.FAIL, "响应报文未获取到");
                }
                //log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp, recordID);
//                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
//                BasicDBObject newDoc = new BasicDBObject("$set",
//                        new BasicDBObject().append("receiveBody", responseBody));
//                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                ctx.setResponseBody(responseBody);
                log.info("入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            } else if (!Objects.equals(null, rspBody)) {
                //log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp, recordID);
//                BasicDBObject searchDoc = new BasicDBObject().append("iD", recordID);
//                BasicDBObject newDoc = new BasicDBObject("$set",
//                        new BasicDBObject().append("receiveBody", rspBody));
//                collection.findOneAndUpdate(searchDoc, newDoc, new FindOneAndUpdateOptions().upsert(true));
                log.info("入库完成");
                ctx.setResponseBody(rspBody);
            } else {
                //log.info("未接收到{}返回的任何数据,记录ID:{}", accessClientIp, recordID);

            }
            ctx.set(GlobalConstants.RECORD_PRIMARY_KEY,recordID);
            ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z, "post过滤器异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s, CodeMsg.FAIL, "内部异常");
        } finally {
            ResponseSafeToSignFilter.getFindlly(inputStream, inputStreamOld, inputStreamNew);
        }
    }
}
