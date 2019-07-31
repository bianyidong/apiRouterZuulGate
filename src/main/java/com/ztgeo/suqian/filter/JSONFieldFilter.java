package com.ztgeo.suqian.filter;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Component
public class JSONFieldFilter extends ZuulFilter {
    private String param = "";

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        String className = this.getClass().getName();
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
        String api_id = httpServletRequest.getHeader("api_id");

        return true;
    }

    @Override
    public Object run() throws ZuulException {

        InputStream stream = RequestContext.getCurrentContext().getResponseDataStream();
        String body = IOUtils.toString(stream);
        System.out.println(body);

        JSONObject backJson = new JSONObject();

        JSONObject json = null;
        try {
            json = null;
            List<String> paramList = Arrays.asList(param.split(","));
            Configuration conf = Configuration.builder().build();
            DocumentContext context = null;
            for (String rule:paramList) {

                if(StringUtils.isEmpty(json)){
                    context = JsonPath.using(conf).parse(body);
                    json = new JSONObject(context.read("$"));
                }

                context = JsonPath.using(conf).parse(json);
                context.delete(rule);
                json = new JSONObject(context.read("$"));
            }

            backJson.put("msg","true");
            backJson.put("data",json);
        } catch (Exception e) {
            e.printStackTrace();
            backJson.put("msg","false");
            if(e.getMessage().contains("Missing property in path")){
                backJson.put("data","JSON过滤规则异常，请先过滤子节点再过滤父节点");
            }

        }


















        RequestContext.getCurrentContext().setResponseBody(body);




        return null;
    }
}
