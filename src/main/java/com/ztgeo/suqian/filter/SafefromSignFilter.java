package com.ztgeo.suqian.filter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.entity.HttpEntity;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;


/**
 * 用于鉴权
 */
@Component
public class SafefromSignFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SafefromSignFilter.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisOperator redis;
    @Autowired
    private MongoClient mongoClient;
    @Value("${customAttributes.dbSafeName}")
    private String dbSafeName; // 存储用户发送数据的数据库名

    @Override
    public Object run() throws ZuulException {
        try {
            log.info("=================进入安全请求方密钥验签过滤器,=====================");
            // 获取request
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            //1.获取heard中的userID和ApiID
            String userID=request.getHeader("form_user");
            //2.获取body中的加密和加签数据并做解密验签
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            JSONObject jsonObject = JSON.parseObject(body);
            String data=jsonObject.get("data").toString();
            String sign=jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到数据或签名");
            //获取redis中的key值
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            JSONObject getjsonObject = JSONObject.parseObject(str);
            String Sign_pub_key=getjsonObject.getString("Sign_pub_key");


            if (StringUtils.isBlank(Sign_pub_key))
                throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到请求方密钥信息");
            // 验证签名
            boolean verifyResult = CryptographyOperation.signatureVerify(Sign_pub_key, data, sign);
            if (Objects.equals(verifyResult, false))
                throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);

            return null;

           } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
        }
    }

    @Override
    public boolean shouldFilter() {
        return getSafeBool(jdbcTemplate,"SafefromSignFilter");
    }
    static boolean getSafeBool(JdbcTemplate jdbcTemplate,String type) {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String apiID=request.getHeader("api_id");
        int count = jdbcTemplate.queryForObject("SELECT COUNT(0) from api_user_filter  where api_id='" + apiID + "' and filter_bc='"+type+"'",Integer.class);
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
