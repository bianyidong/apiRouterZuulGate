package com.ztgeo.suqian.filter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;


import java.io.InputStream;

import java.nio.charset.Charset;

import java.util.Objects;
import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;


/**
 * 用于验签请求方
 */
@Component
public class SafefromSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafefromSignFilter.class);
    //访问用户Id
    private String from_user;
    private String uri;
    private String Sign_pub_key;

    @Resource
    private ApiJgtoPtFilterRepository apiJgtoPtFilterRepository;
//    @Autowired
//    private RedisOperator redis;
//    @Resource
//    private UserKeyInfoRepository userKeyInfoRepository;
//    @Resource
//    private ApiUserFilterRepository apiUserFilterRepository;

    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入安全请求方密钥验签过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            //1.获取heard中的userID
            String userID=request.getHeader("from_user");
            //2.获取body中的加密和加签数据并验签
            InputStream in = request.getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign)) {
                log.info("参数错误，未获取到数据或签名");
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到安全请求方密钥验签过滤器数据或签名");
            }
            //获取redis中的key值
//            String str = redis.get(USER_REDIS_SESSION +":"+userID);
//            if (StringUtils.isBlank(str)){
//                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(userID);
//                Sign_pub_key=userKeyInfo.getSignPubKey();
//                JSONObject setjsonObject = new JSONObject();
//                setjsonObject.put("Symmetric_pubkey",userKeyInfo.getSymmetricPubkey());
//                setjsonObject.put("Sign_secret_key", userKeyInfo.getSignSecretKey());
//                setjsonObject.put("Sign_pub_key",userKeyInfo.getSignPubKey());
//                setjsonObject.put("Sign_pt_secret_key",userKeyInfo.getSignPtSecretKey());
//                setjsonObject.put("Sign_pt_pub_key",userKeyInfo.getSignPtPubKey());
//                //存入Redis
//                redis.set(USER_REDIS_SESSION +":"+userID, setjsonObject.toJSONString());
//            }else {
//                JSONObject getjsonObject = JSONObject.parseObject(str);
//                Sign_pub_key=getjsonObject.getString("Sign_pub_key");
//                if (StringUtils.isBlank(Sign_pub_key)){
//                     throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到安全请求方密钥验签过滤器密钥信息");
//                }
//            }

            ApiJgtoPtFilter apiJgtoPtFilter =apiJgtoPtFilterRepository.queryApiJgtoPtFilterByFromUserAndUriAndFilterBc(userID,uri);
            Sign_pub_key=apiJgtoPtFilter.getPubKey();
            // 验证签名
            boolean verifyResult = CryptographyOperation.signatureVerify(Sign_pub_key, data, sign);
            if (Objects.equals(verifyResult, false))
                throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);

            ctx.set(GlobalConstants.SENDBODY, body);
            return null;

           } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FROMSIGN_ERROR);
        }
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
    public int filterOrder() {
        return 0;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }


}
