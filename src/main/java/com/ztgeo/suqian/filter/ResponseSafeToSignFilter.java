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
import com.ztgeo.suqian.entity.ag_datashare.ApiBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.ApiBaseInfoRepository;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;


/**
 * 响应验签过滤器
 *
 * @author bianyidong
 * @version 2019-6-21
 */
@Component
public class ResponseSafeToSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseSafeToSignFilter.class);
    private String api_id;
    private String Sign_pub_keyapiUserIDJson;
//    @Resource
//    private UserKeyInfoRepository userKeyInfoRepository;
//    @Autowired
//    private RedisOperator redis;
    @Resource
    private ApiUserFilterRepository apiUserFilterRepository;
    @Resource
    private ApiBaseInfoRepository apiBaseInfoRepository;

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
        log.info("=================进入post返回安全验签过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getResponseDataStream();
            String apiID=ctx.getRequest().getHeader("api_id");
            //获取记录主键ID(来自routing过滤器保存的上下文)
            Object recordID = ctx.get(GlobalConstants.RECORD_PRIMARY_KEY);
            Object accessClientIp = ctx.get(GlobalConstants.ACCESS_IP_KEY);
            if (Objects.equals(null, accessClientIp) || Objects.equals(null, recordID))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "返回安全验签过滤器访问者IP或记录ID未获取到");
            //获取接收方机构的密钥
//            List<ApiBaseInfo> list =apiBaseInfoRepository.findApiBaseInfosByApiIdEquals(apiID);
//            ApiBaseInfo apiBaseInfo=list.get(0);
//            String apiUserID = redis.get(USER_REDIS_SESSION +":"+apiBaseInfo.getApiOwnerId());
//            if (StringUtils.isBlank(apiUserID)){
//                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(apiBaseInfo.getApiOwnerId());
//                Sign_pub_keyapiUserIDJson=userKeyInfo.getSignPubKey();
//                JSONObject setjsonObject = new JSONObject();
//                setjsonObject.put("Symmetric_pubkey",userKeyInfo.getSymmetricPubkey());
//                setjsonObject.put("Sign_secret_key", userKeyInfo.getSignSecretKey());
//                setjsonObject.put("Sign_pub_key",userKeyInfo.getSignPubKey());
//                setjsonObject.put("Sign_pt_secret_key",userKeyInfo.getSignPtSecretKey());
//                setjsonObject.put("Sign_pt_pub_key",userKeyInfo.getSignPtPubKey());
//                //存入Redis
//                redis.set(USER_REDIS_SESSION +":"+apiBaseInfo.getApiOwnerId(), setjsonObject.toJSONString());
//            }else {
//                JSONObject getjsonObject = JSONObject.parseObject(apiUserID);
//                Sign_pub_keyapiUserIDJson=getjsonObject.getString("Sign_pub_key");
//                if (StringUtils.isBlank(Sign_pub_keyapiUserIDJson)){
//                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到返回安全验签过滤器密钥信息");
//                }
//            }
            ApiBaseInfo apiBaseInfo=apiBaseInfoRepository.queryApiBaseInfoByApiId(apiID);
            Sign_pub_keyapiUserIDJson=apiBaseInfo.getSignPubKey();
            String rspBody = ctx.getResponseBody();
            if(!Objects.equals(null,rspBody)){
                JSONObject jsonObject = JSON.parseObject(rspBody);
                String data=jsonObject.get("data").toString();
                String sign=jsonObject.get("sign").toString();

                // 验证签名
                boolean rspVerifyResult = CryptographyOperation.signatureVerify(Sign_pub_keyapiUserIDJson, data, sign);
                if (Objects.equals(rspVerifyResult, false))
                    throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
                log.info("返回安全验签过滤器入库完成");
                ctx.setResponseBody(rspBody);
            }else if(!Objects.equals(null,inputStream)){
                // 获取返回的body
                ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
                inputStreamOld = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 原始流
                inputStreamNew = new ByteArrayInputStream(byteArrayOutputStream.toByteArray()); // 复制流
                // 获取返回的body字符串
                String responseBody = StreamUtils.copyToString(inputStreamOld, StandardCharsets.UTF_8);
                if (Objects.equals(null, responseBody)){
                    responseBody = "";
                    throw new ZtgeoBizZuulException(CodeMsg.FAIL,"返回安全验签过滤器响应报文未获取到");
                }
                JSONObject jsonresponseBody = JSON.parseObject(responseBody);
                String rspEncryptData=jsonresponseBody.get("data").toString();
                String rspSignData=jsonresponseBody.get("sign").toString();
                // 验证签名
                boolean rspVerifyResult = CryptographyOperation.signatureVerify(Sign_pub_keyapiUserIDJson, rspEncryptData, rspSignData);
                if (Objects.equals(rspVerifyResult, false))
                    throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
                jsonresponseBody.put("data",rspEncryptData);
                jsonresponseBody.put("sign",rspSignData);
                String newbody=jsonresponseBody.toString();
                ctx.setResponseBody(newbody);
                log.info("返回安全验签过滤器入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            }else {
                log.info("返回安全验签过滤器记录完成");
            }
            ctx.set(GlobalConstants.RECORD_PRIMARY_KEY,recordID);
            ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z,"post返回安全验签过滤器异常", z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s,CodeMsg.RSPSIGN_ERROR, "内部异常");
        } finally {
            getFindlly(inputStream, inputStreamOld, inputStreamNew);
        }
    }

    static void getFindlly(InputStream inputStream, InputStream inputStreamOld, InputStream inputStreamNew) {
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
