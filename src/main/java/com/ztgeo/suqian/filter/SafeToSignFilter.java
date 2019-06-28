package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;
import static com.ztgeo.suqian.filter.SafefromDataFilter.getObject;



/**
 * 用于鉴权
 */
@Component
public class SafeToSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafeToSignFilter.class);
    private String api_id;
    private String Sign_pt_secret_key;
    @Resource
    private UserKeyInfoRepository userKeyInfoRepository;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
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
            String sendbody=ctx.get(GlobalConstants.SENDBODY).toString();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");

            //2.获取body中的重新加密后的数据
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到数据或签名");
            //获取redis中的key值即密钥信息
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            if (StringUtils.isBlank(str)){
                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(userID);
                Sign_pt_secret_key=userKeyInfo.getSignPtSecretKey();
                JSONObject setjsonObject = new JSONObject();
                setjsonObject.put("Symmetric_pubkey",userKeyInfo.getSymmetricPubkey());
                setjsonObject.put("Sign_secret_key", userKeyInfo.getSignSecretKey());
                setjsonObject.put("Sign_pub_key",userKeyInfo.getSignPubKey());
                setjsonObject.put("Sign_pt_secret_key",userKeyInfo.getSignPtSecretKey());
                setjsonObject.put("Sign_pt_pub_key",userKeyInfo.getSignPtPubKey());
                //存入Redis
                redis.set(USER_REDIS_SESSION +":"+userID, setjsonObject.toJSONString());
            }else {
                JSONObject getjsonObject = JSONObject.parseObject(str);
                Sign_pt_secret_key=getjsonObject.getString("Sign_pt_secret_key");
                if (StringUtils.isBlank(Sign_pt_secret_key)){
                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到请求方密钥信息");
                }
            }
            //3.重新加签
             String receiveSign = CryptographyOperation.generateSign(Sign_pt_secret_key, data);
            //4.重新加载到requset中
            jsonObject.put("data",data);
            jsonObject.put("sign",receiveSign);
            String newbody=jsonObject.toString();
            ctx.set(GlobalConstants.SENDBODY, newbody);

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
    public int filterOrder() {
        return 4;
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }


}
