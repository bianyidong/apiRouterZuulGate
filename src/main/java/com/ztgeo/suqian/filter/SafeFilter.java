package com.ztgeo.suqian.filter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
import jdk.management.resource.internal.inst.SocketOutputStreamRMHooks;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;


/**
 * 用于鉴权
 */
@Component
public class SafeFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafeFilter.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisOperator redis;


    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入安全密钥数据验证过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            log.info("访问者IP:{}", HttpUtils.getIpAdrress(request));
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");
            String apiID=request.getHeader("api_id");
            System.out.println(apiID);
            //2.获取body中的加密和加签数据并做解密验签
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            System.out.println("body:" + body);
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            System.out.println(data);
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到数据或签名");

            //获取redis中的key值
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            JSONObject getjsonObject = JSONObject.parseObject(str);
            String Symmetric_pubkey=getjsonObject.getString("Symmetric_pubkey");
            String Sign_secret_key = getjsonObject.getString("Sign_secret_key");
            String Sign_pub_key=getjsonObject.getString("Sign_pub_key");
            String Sign_pt_secret_key=getjsonObject.getString("Sign_pt_secret_key");
            String Sign_pt_pub_key=getjsonObject.getString("Sign_pt_pub_key");

            // 验证签名
            boolean verifyResult = CryptographyOperation.signatureVerify(Sign_pub_key, data, sign);
            if (Objects.equals(verifyResult, false))
                throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
            // 解密数据
            String reqDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkey, data);

            List<ApiBaseInfo> apiBaseInfo = jdbcTemplate.query(" select abi.api_owner_id FROM api_base_info abi where abi.api_id ='" + apiID + "'",new BeanPropertyRowMapper<>(ApiBaseInfo.class));
            System.out.println(apiBaseInfo.get(0).getApi_owner_id());
            String apiUserID = redis.get(USER_REDIS_SESSION +":"+apiBaseInfo.get(0).getApi_owner_id());
            JSONObject apiUserIDJson  = JSONObject.parseObject(apiUserID);
            String Symmetric_pubkeyapiUserIDJson=apiUserIDJson.getString("Symmetric_pubkey");
            //String Sign_secret_keyapiUserIDJson = apiUserIDJson.getString("Sign_secret_key");
            String Sign_pub_keyapiUserIDJson=apiUserIDJson.getString("Sign_pub_key");
            String Sign_pt_secret_keyapiUserIDJson=apiUserIDJson.getString("Sign_pt_secret_key");
            String Sign_pt_pub_keyapiUserIDJson=apiUserIDJson.getString("Sign_pt_pub_key");
            if (StringUtils.isBlank(Sign_pub_keyapiUserIDJson) || StringUtils.isBlank(Symmetric_pubkeyapiUserIDJson))
                throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到接收方密钥信息");
            //重新加密加签
            String receiveEncryptData = CryptographyOperation.aesEncrypt(Symmetric_pubkeyapiUserIDJson, reqDecryptData);
            String receiveSign = CryptographyOperation.generateSign(Sign_pt_secret_key, receiveEncryptData);
            //重新加载到requset中
            jsonObject.put("data",receiveEncryptData);
            jsonObject.put("sign",receiveSign);
            String newbody=jsonObject.toString();
            System.out.println("newbody:"+newbody);
            final byte[] reqBodyBytes = newbody.getBytes();
            ctx.setRequest(new HttpServletRequestWrapper(request){
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(reqBodyBytes);
                }
                @Override
                public int getContentLength() {
                    return reqBodyBytes.length;
                }
                @Override
                public long getContentLengthLong() {
                    return reqBodyBytes.length;
                }
            });
            return null;
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
        }
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String apiID=request.getHeader("api_id");
        int count = jdbcTemplate.queryForObject("SELECT COUNT(0) from api_user_filter  where api_id='" + apiID + "' and filter_bc='SafeFilter'",Integer.class);
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
        return FilterConstants.ROUTE_TYPE;
    }


}
