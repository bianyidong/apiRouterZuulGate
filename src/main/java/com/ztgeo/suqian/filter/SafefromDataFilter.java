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
import com.ztgeo.suqian.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.Charset;


import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;
import static com.ztgeo.suqian.filter.AddSendBodyFilter.getObject;


/**
 * 用于解密请求方
 */
@Component
public class SafefromDataFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafefromDataFilter.class);
    private String from_user;
    private String Symmetric_pubkey;
    private String uri;
//    @Resource
//    private UserKeyInfoRepository userKeyInfoRepository;
//    @Autowired
//    private RedisOperator redis;
    @Resource
    private ApiJgtoPtFilterRepository apiJgtoPtFilterRepository;


    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入安全密钥请求方解密验证过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            //String sendbody=ctx.get(GlobalConstants.SENDBODY).toString();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
            //1.获取heard中的userID
            String userID=request.getHeader("from_user");
            //2.获取body中的加密和加签数据并做解密
            InputStream in = request.getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到安全密钥请求方解密验证过滤器数据或签名");
            //获取redis中的key值
//            String str = redis.get(USER_REDIS_SESSION +":"+userID);
//            if (StringUtils.isBlank(str)){
//                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(userID);
//                Symmetric_pubkey=userKeyInfo.getSymmetricPubkey();
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
//                Symmetric_pubkey=getjsonObject.getString("Symmetric_pubkey");
//                if (StringUtils.isBlank(Symmetric_pubkey)){
//                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到安全密钥请求方解密验证过滤器密钥信息");
//                }
//            }
            ApiJgtoPtFilter apiJgtoPtFilter =apiJgtoPtFilterRepository.queryApiJgtoPtFilterByFromUserAndUriAndFilterBc(userID,uri);
            Symmetric_pubkey=apiJgtoPtFilter.getSymPubkey();
            // 解密数据
            String reqDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkey, data);
            //重新加载到requset中
            jsonObject.put("data",reqDecryptData);
            jsonObject.put("sign",sign);
            String newbody=jsonObject.toString();
            ctx.set(GlobalConstants.SENDBODY, newbody);
           return getObject(ctx, request, newbody);

        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FROMDATA_ERROR);
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
        return 1;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }


}
