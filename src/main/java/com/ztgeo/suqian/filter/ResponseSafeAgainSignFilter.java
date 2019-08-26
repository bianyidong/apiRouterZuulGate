package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ag_datashare.ApiJgtoPtFilter;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiJgtoPtFilterRepository;
import com.ztgeo.suqian.repository.ApiUserFilterRepository;
import com.ztgeo.suqian.repository.UserKeyInfoRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.Objects;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;

/**
 * 响应重新加签过滤器
 *
 * @author bianyidong
 * @version 2019-6-25
 */
@Component
public class ResponseSafeAgainSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseSafeAgainSignFilter.class);
    private String from_user;
    private String Sign_pt_secret_key;
    private  String uri;
    @Resource
    private ApiJgtoPtFilterRepository apiJgtoPtFilterRepository;
//    @Resource
//    private UserKeyInfoRepository userKeyInfoRepository;
//    @Resource
//    private ApiUserFilterRepository apiUserFilterRepository;
//    @Autowired
//    private RedisOperator redis;
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 4;
    }

    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getSimpleName();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        from_user=request.getHeader("from_user");
        uri = request.getRequestURI();
        int count = apiJgtoPtFilterRepository.countApiJgtoPtFilterByFromUserAndUriAndFilterBc(from_user,uri,className);
        if (count>0){
            return true;
        }else {
            return false;
        }
    }



    @Override
    public Object run() throws ZuulException {
        log.info("=================进入post返回安全重新加签过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getResponseDataStream();
            String userID=ctx.getRequest().getHeader("from_user");
            //获取记录主键ID(来自routing过滤器保存的上下文)
            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "返回重新加签名过滤器访问者IP或记录ID未获取到");
            //获取redis中userID的key值
//            String str = redis.get(USER_REDIS_SESSION +":"+userID);
//            if (StringUtils.isBlank(str)){
//                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(userID);
//                Sign_pt_secret_key=userKeyInfo.getSignPtSecretKey();
//                JSONObject setjsonObject = new JSONObject();
//                setjsonObject.put("Symmetric_pubkey",userKeyInfo.getSymmetricPubkey());
//                setjsonObject.put("Sign_secret_key", userKeyInfo.getSignSecretKey());
//                setjsonObject.put("Sign_pub_key",userKeyInfo.getSignPubKey());
//                setjsonObject.put("Sign_pt_secret_key",userKeyInfo.getSignPtSecretKey());
//                setjsonObject.put("Sign_pt_pub_key",userKeyInfo.getSignPtPubKey());
//                //存入Redis
//                redis.set(USER_REDIS_SESSION +":"+userID, setjsonObject.toJSONString());
//            }else {
//                JSONObject getJsonObject = JSONObject.parseObject(str);
//                Sign_pt_secret_key=getJsonObject.getString("Sign_pt_secret_key");
//                if (StringUtils.isBlank(Sign_pt_secret_key)){
//                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到返回重新加签名密钥信息");
//                }
//            }
            ApiJgtoPtFilter apiJgtoPtFilter =apiJgtoPtFilterRepository.queryApiJgtoPtFilterByFromUserAndUriAndFilterBc(userID,uri);
            Sign_pt_secret_key=apiJgtoPtFilter.getPtSecretKey();
            String rspBody = ctx.getResponseBody();
             if(!Objects.equals(null,rspBody)){
                JSONObject jsonObject = JSON.parseObject(rspBody);
                String data=jsonObject.get("data").toString();

                String  sign = CryptographyOperation.generateSign(Sign_pt_secret_key, data);
                //重新加载到response中
                jsonObject.put("sign",sign);
                String newbody=jsonObject.toString();
                log.info("返回重新加签名过滤器入库完成");
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
                    throw new ZtgeoBizZuulException(CodeMsg.FAIL,"返回安全重新加签过滤器响应报文未获取到");
                }
                JSONObject jsonresponseBody = JSON.parseObject(responseBody);
                String rspEncryptData=jsonresponseBody.get("data").toString();
                String rspSignData=jsonresponseBody.get("sign").toString();

                // 重新加签
                rspSignData = CryptographyOperation.generateSign(Sign_pt_secret_key, rspEncryptData);
                jsonresponseBody.put("sign",rspSignData);
                String newbody=jsonresponseBody.toString();
                ctx.setResponseBody(newbody);
                log.info("返回重新加签名过滤器入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            }else {
                log.info("返回重新加签名过滤器记录完成");
            }
            ctx.set(GlobalConstants.RECORD_PRIMARY_KEY,recordID);
            ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z,"返回重新加签名过滤器Post异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s,CodeMsg.AGARSPSIGN_ERROR, "内部异常");
        } finally {
            ResponseSafeToSignFilter.getFindlly(inputStream, inputStreamOld, inputStreamNew);
        }
    }
}
