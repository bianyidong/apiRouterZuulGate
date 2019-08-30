package com.ztgeo.suqian.filter.dz;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ag_datashare.DzYixing;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.repository.DzYixingRepository;
import com.ztgeo.suqian.utils.StreamOperateUtils;
import com.ztgeo.suqian.utils.XmlAndJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
/**
 * 宜兴地税定制---响应
 */
@Component
public class YXLTRespDZFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private DzYixingRepository dzYixingRepository;

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return -88;
    }

    @Override
    public boolean shouldFilter() {
        /**
         * 宜兴地税定制过滤器
         * 因为定制过滤器在请求时修改了真正的转发地址，在响应中应该从ctx中获取APIID进行判断
         */
        // 获取当前请求
        RequestContext ctx = RequestContext.getCurrentContext();
        Object dzObj = ctx.get("api_id");
        if(StringUtils.isEmpty(dzObj)){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Object run() throws ZuulException {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest httpServletRequest = ctx.getRequest();
            String requestURI = httpServletRequest.getRequestURI();

            InputStream inputStream = ctx.getResponseDataStream();
            ByteArrayOutputStream byteArrayOutputStream = StreamOperateUtils.cloneInputStreamToByteArray(inputStream);
            String responseBody = StreamUtils.copyToString(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), StandardCharsets.UTF_8);
            log.info("响应体：" + responseBody);

            // utf8 -- gbk,字段大写
            String xml = XmlAndJsonUtils.json2xml_UpperCase(responseBody);
            xml = xml.replaceAll("utf-8", "GBK");
            xml = xml.replaceAll("UTF-8", "GBK");
            log.info("xml:"+xml);


            // 增加jstl3BizPackage版本号
//            xml = xml.replaceAll("<jslt3BizPackage>", "<jslt3BizPackage version=\"1.0\">");
//            log.info("转换XML：" + xml);

//            // 对<>进行转义
//            xml = xml.replaceAll("<", "&lt;");
//            xml = xml.replaceAll(">", "&gt;");

            // 通过请求地址再次将定制实例查询
            DzYixing dzYixing = dzYixingRepository.findDzYixingsByApiIdEquals(ctx.get("api_id").toString());
            String soapbodyResp = dzYixing.getSoapbodyResp();
            String realRespString = soapbodyResp.replaceAll("###", xml);
            log.info("realResp：" + realRespString);

            ctx.setResponseBody(realRespString);

        } catch (IOException e) {
            throw new ZtgeoBizZuulException(CodeMsg.YXLT_DZ_RESP_ERROR);
        }
        return null;
    }

}
