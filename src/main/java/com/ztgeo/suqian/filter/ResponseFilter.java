package com.ztgeo.suqian.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 响应过滤器
 *
 * @author zoupeidong
 * @version 2018-12-7
 */
public class ResponseFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ResponseFilter.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        log.info("=================进入post过滤器,接收返回的数据=====================");
        InputStream inputStream = null;
        InputStream inputStreamOld = null;
        InputStream inputStreamNew = null;
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            inputStream = ctx.getResponseDataStream();
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
                log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                jdbcTemplate.update("update api_access_record aar set aar.response_data = ? where aar.id = ?", new Object[]{responseBody, recordID});
                log.info("入库完成");
                ctx.setResponseDataStream(inputStreamNew);
            }else if(!Objects.equals(null,rspBody)){
                log.info("接收到{}返回的数据,正在入库,记录ID:{}", accessClientIp,recordID);
                jdbcTemplate.update("update api_access_record aar set aar.response_data = ? where aar.id = ?", new Object[]{rspBody, recordID});
                log.info("入库完成");
                ctx.setResponseBody(rspBody);
            }else {
                log.info("未接收到{}返回的任何数据,记录ID:{}", accessClientIp,recordID);
                jdbcTemplate.update("update api_access_record aar set aar.response_data = ? where aar.id = ?", new Object[]{"", recordID});
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
