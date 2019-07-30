package com.ztgeo.suqian.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.CryptographyOperation;

import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.config.RedisOperator;

import com.ztgeo.suqian.entity.ag_datashare.NoticeBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.NoticeRecord;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.msg.ResultMap;
import com.ztgeo.suqian.repository.NoticeBaseInfoRepository;
import com.ztgeo.suqian.repository.NoticeRecordRepository;
import com.ztgeo.suqian.repository.UserKeyInfoRepository;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;

/**
 * 通知控制器
 */
@RestController
public class NoticeController {
    private static final Logger log = LoggerFactory.getLogger(RouteController.class);
    private String Symmetric_pubkey;

    private String SignPubKey;
    @Resource
    private NoticeBaseInfoRepository noticeBaseInfoRepository;
    @Resource
    private UserKeyInfoRepository userKeyInfoRepository;
    @Resource
    private NoticeRecordRepository noticeRecordRepository;
    @Autowired
    private RedisOperator redis;
    /**
     * 发送通知
     */
    @RequestMapping(value = "/ztgeoNotice", method = RequestMethod.POST)
    public String sendNotice(HttpServletRequest request, @RequestBody String bodyStr) throws ZuulException {

        try {
            // 1.查询发送者ID和待发送的通知类型
            String userID = request.getHeader("from_user");
            String noticeCode = request.getHeader("api_id");
            RequestContext ctx = RequestContext.getCurrentContext();
            //2.获取body中的加密和加签数据并做解密
            JSONObject jsonObject = JSON.parseObject(bodyStr);
            String data = jsonObject.get("data").toString();
            String sign = jsonObject.get("sign").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(sign))
                throw new ZtgeoBizZuulException(CodeMsg.PARAMS_ERROR, "未获取到通知数据或签名");
            //获取redis中的key值
            String str = redis.get(USER_REDIS_SESSION +":"+userID);
            if (StringUtils.isBlank(str)){
                UserKeyInfo userKeyInfo=userKeyInfoRepository.findByUserRealIdEquals(userID);
                Symmetric_pubkey=userKeyInfo.getSymmetricPubkey();
                SignPubKey=userKeyInfo.getSignPubKey();
                JSONObject setjsonObject = new JSONObject();
                setjsonObject.put("Symmetric_pubkey",userKeyInfo.getSymmetricPubkey());
                setjsonObject.put("Sign_secret_key", userKeyInfo.getSignSecretKey());
                setjsonObject.put("Sign_pub_key",userKeyInfo.getSignPubKey());
                setjsonObject.put("Sign_pt_secret_key",userKeyInfo.getSignPtSecretKey());
                setjsonObject.put("Sign_pt_pub_key",userKeyInfo.getSignPtPubKey());
                //存入Redis
                redis.set(USER_REDIS_SESSION +":"+userID, setjsonObject.toJSONString());
            }else {
                JSONObject getjsonObject = JSONObject.parseObject(str);
                Symmetric_pubkey=getjsonObject.getString("Symmetric_pubkey");
                SignPubKey=getjsonObject.getString("Sign_pub_key");
                if (StringUtils.isBlank(Symmetric_pubkey)){
                    log.info("未查询到通知请求方密钥信息");
                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到通知请求方密钥信息");
                }
            }
            // 验证签名
            boolean verifyResult = CryptographyOperation.signatureVerify(SignPubKey, data, sign);
            if (Objects.equals(verifyResult, false)) {
                log.info("通知请求方验签失败");
                throw new ZtgeoBizRuntimeException(CodeMsg.SIGN_ERROR);
            }
            // 解密数据
            String reqDecryptData = CryptographyOperation.aesDecrypt(Symmetric_pubkey, data);
            // 查询待发送的http列表
            List<NoticeBaseInfo> urlList = noticeBaseInfoRepository.querySendUrl(userID, noticeCode);
            // 异步发送http请求
            for (int i = 0; i < urlList.size(); i++) {
                //接收方真实ID
                String receiverId = urlList.get(i).getUserRealId();
                // 获取接收方机构的密钥
                UserKeyInfo userKeyInfoReceive =userKeyInfoRepository.findByUserRealIdEquals(receiverId);
                String receiveSignPtSecretKey = userKeyInfoReceive.getSignPtSecretKey();
                String receiveAesKey = userKeyInfoReceive.getSymmetricPubkey();
                //发送地址
                String url = urlList.get(i).getNoticePath();
                //记录ID
                String id= com.ztgeo.suqian.utils.StringUtils.getShortUUID();
                //接收方用户名（例如bdc_dj）
                String receiverName = urlList.get(i).getUsername();
                //接收方机构名
                String name = urlList.get(i).getName();
                String typedesc = urlList.get(i).getNoticeNote();
                LocalDateTime localDateTime = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateTimeFormatter.format(localDateTime);
                if (StringUtils.isBlank(receiveSignPtSecretKey) || StringUtils.isBlank(receiveAesKey))
                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未查询到要通知接收方密钥信息");
                // 重新加密加签
                String receiveEncryptData = CryptographyOperation.aesEncrypt(receiveAesKey, reqDecryptData);
                String receiveSign = CryptographyOperation.generateSign(receiveSignPtSecretKey, receiveEncryptData);
                //重新加载到requset中
                jsonObject.put("data",receiveEncryptData);
                jsonObject.put("sign",receiveSign);
                String sendStr=jsonObject.toString();

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build();
                okhttp3.RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                        , sendStr);
                Request requestHttp = new Request.Builder()
                    .url(url)//请求的url
                        .post(requestBody)
                        .build();
                Call call = okHttpClient.newCall(requestHttp);
                call.enqueue(new Callback() {
                    //请求失败执行的方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        // 数据
                        noticeRecordRepository.save(new NoticeRecord(id,userID,receiverId,url,receiverName,name,noticeCode,typedesc,1,currentTime,0,sendStr));
                    }
                    //请求成功执行的方法
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        response.body().string();//
                        if (response.isSuccessful()) { // 成功响应
                            noticeRecordRepository.save(new NoticeRecord(id,userID,receiverId,url,receiverName,name,noticeCode,typedesc,0,currentTime,0,sendStr));
                        } else { // 失败
                            noticeRecordRepository.save(new NoticeRecord(id,userID,receiverId,url,receiverName,name,noticeCode,typedesc,1,currentTime,0,sendStr));
                          throw new ZtgeoBizRuntimeException(CodeMsg.RECEIVE_EXCEPTION, "请联系相关人员");
                        }

                    }
                });
            }
            return ResultMap.ok(CodeMsg.SUCCESS).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "通知功能内部异常");
        }

    }

}
