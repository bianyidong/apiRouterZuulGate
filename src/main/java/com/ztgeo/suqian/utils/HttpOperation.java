package com.ztgeo.suqian.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.filter.AddSendBodyFilter;
import com.ztgeo.suqian.msg.CodeMsg;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 处理http请求操作
 *
 * @author zoupeidong
 * @version 2018-12-13
 */
public class HttpOperation {
    private static Logger log = LoggerFactory.getLogger(HttpOperation.class);

    public static String sendPostByApplicationXwwwFromUrlendcoded(String url,String params){
        String respBody = null;
        try {
        HttpResponse<String> response = Unirest.post(url)
                .header("content-type", "application/x-www-form-urlencoded")
                .header("cache-control", "no-cache")
                .body(params).asString();

        int status = response.getStatus();
        if(status == 200) {
            respBody = response.getBody();
        }else{
            log.info("获取TOKEN失败，状态码：" + status);
            throw new ZtgeoBizRuntimeException(CodeMsg.SDK_INTER_ERROR);
        }

        } catch (Exception e) {
            log.info("调用sendPostByApplicationXwwwFromUrlendcoded方法异常！",e);
            throw new ZtgeoBizRuntimeException(CodeMsg.SDK_INTER_ERROR);
        }
        return respBody;
    }


    /**
     * 发送http请求
     * get方式
     *
     * @param url 待发送数据
     * @return 返回的数据
     */
    public static String sendGetHttp(String url) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().get().url(url).build();//请求的url
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZtgeoBizRuntimeException(CodeMsg.SDK_INTER_ERROR);
        }
    }

    /**
     * 发送http请求
     * content-type:application/json
     *
     * @param url  请求url
     * @param data 待发送数据
     * @return 返回的数据
     */
    public static String sendJsonHttp(String url, String data) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , data);//发送的数据
            Request request = new Request.Builder()
                    .url(url)//请求的url
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZtgeoBizRuntimeException(CodeMsg.SDK_INTER_ERROR);
        }
    }

}
