package com.ztgeo.suqian.filter;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

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
            // 解密数据
            String reqDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkey, data);
            // 验证签名
            boolean verifyResult = CryptographyOperation.signatureVerify(Sign_pub_key, reqDecryptData, sign);
            List<ApiBaseInfo> apiBaseInfo = jdbcTemplate.query(" select abi.api_owner_id FROM api_base_info abi where abi.api_id ='" + apiID + "'",new BeanPropertyRowMapper<>(ApiBaseInfo.class));
            System.out.println(apiBaseInfo.get(0).getApi_owner_id());
            String apiUserID = redis.get(USER_REDIS_SESSION +":"+apiBaseInfo.get(0).getApi_owner_id());
            JSONObject apiUserIDJson  = JSONObject.parseObject(apiUserID);
            String Symmetric_pubkeyapiUserIDJson=getjsonObject.getString("Symmetric_pubkey");
            String Sign_secret_keyapiUserIDJson = apiUserIDJson.getString("Sign_secret_key");
            String Sign_pub_keyapiUserIDJson=apiUserIDJson.getString("Sign_pub_key");
            String Sign_pt_secret_keyapiUserIDJson=apiUserIDJson.getString("Sign_pt_secret_key");
            String Sign_pt_pub_keyapiUserIDJson=apiUserIDJson.getString("Sign_pt_pub_key");
            //重新加密加签
            String receiveEncryptData = CryptographyOperation.aesEncrypt(Symmetric_pubkeyapiUserIDJson, reqDecryptData);
            String receiveSign = CryptographyOperation.generateSign(Sign_pt_secret_key, receiveEncryptData);
            //重新加载到requset中

//            jsonObject.get("token").toString();
//            JSONObject jsonObject1 = JSON.parseObject(jsonObject.get("token").toString());
//            System.out.println(jsonObject1.get("userID"));
//            String userId=jsonObject1.get("userID").toString();
//            String apiId=jsonObject1.get("apiID").toString();
//           int cout=  jdbcTemplate.queryForObject("select COUNT(0) from api_user_rel t where t.api_id='"+apiId+"' and  t.user_id='"+userId+"'",Integer.class);
//           if(cout<=0){
//               log.info("访问者:{}无权限访问接口，请开通权限",userId);
//               throw new ZtgeoBizZuulException(CodeMsg.NOT_FOUNDUSER);
//           }

            // 检验是否在IP黑名单中
//            ListOperations<String,Object> listOperations = redisTemplate.opsForList();
//            List<Object> list = listOperations.range("ip-black-list",0,-1);
//            String realIp = HttpUtils.getIpAdrress(request);
//            boolean ipContains = list.contains(realIp);
//            if(ipContains){
//                log.info("访问者IP:{}已被拉黑，拒绝访问", HttpUtils.getIpAdrress(request));
//                throw new ZtgeoBizZuulException(CodeMsg.BLACK_USER);
//            }
            return null;
       // }
//        catch (ZuulException z) {
//            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
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
