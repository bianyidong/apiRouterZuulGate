package com.ztgeo.suqian.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.CryptographyOperation;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.NoticeBaseInfo;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.msg.ResultMap;
import com.ztgeo.suqian.utils.HttpUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通知控制器
 */
@RestController
public class NoticeController {

    private JdbcTemplate jdbcTemplate;

    /**
     *  发送通知
     */
    @RequestMapping(value = "/ztgeoNotice",method = RequestMethod.POST)
    public String sendNotice(HttpServletRequest request,@RequestBody String bodyStr) throws ZuulException{

        try {

        // 1.查询发送者ID和待发送的通知类型
        String userID = request.getHeader("from_user");
        String noticeCode = request.getHeader("api_id");
        RequestContext ctx = RequestContext.getCurrentContext();
        //2.获取body中的加密和加签数据并做解密
        JSONObject jsonObject = JSON.parseObject(bodyStr);
        String sendStr=jsonObject.get("data").toString();
        String sign=jsonObject.get("sign").toString();
//            String sendStr="";
        // 查询待发送的http列表
        List<NoticeBaseInfo> urlList =jdbcTemplate.query("select nbi.notice_path noticePath,nbi.user_real_id userRealId from notice_base_info nbi inner join notice_user_rel nur on nbi.notice_id = nur.notice_id inner join user_key_info uki on nur.user_real_id = uki.user_real_id where uki.user_identity_id = ? and nur.type_id = ?",new Object[]{userID,noticeCode},new RowMapper<NoticeBaseInfo>(){
            @Nullable
            @Override
            public NoticeBaseInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                NoticeBaseInfo noticeBaseInfo = new NoticeBaseInfo();
                String noticePath = rs.getString("noticePath");
                String userRealId = rs.getString("userRealId");
//                noticeBaseInfo.setNoticeNote(noticePath);
//                noticeBaseInfo.setUserRealId(userRealId);
                System.out.println("通知信息"+noticeBaseInfo);
                System.out.println("通知信息"+noticeBaseInfo);
                return noticeBaseInfo;
            }
        });
        // 异步发送http请求
        for (int i = 0; i < urlList.size(); i++) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            okhttp3.RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , sendStr);
           // String url = urlList.get(i).getNoticePath();
            Request requestHttp = new Request.Builder()
//                    .url(url)//请求的url
//                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(requestHttp);
            //String receiverId = urlList.get(0).getUserRealId(); // 接收者ID
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    jdbcTemplate.update("insert into notice_record values(?,?,?,?,?,?)",new PreparedStatementSetter(){
                        @Override
                        public void setValues(PreparedStatement ps) throws SQLException {
                           // ps.setString(1,UUIDUtils.generateShortUuid());
                            ps.setString(2,userID);
//                            ps.setString(3,receiverId);
//                            ps.setString(4,url);
                            ps.setInt(5,1);
                            ps.setString(6,dateTimeFormatter.format(LocalDateTime.now()));
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    jdbcTemplate.update("insert into notice_record values(?,?,?,?,?,?)",new PreparedStatementSetter(){
                        @Override
                        public void setValues(PreparedStatement ps) throws SQLException {
                          //  ps.setString(1,UUIDUtils.generateShortUuid());
                            ps.setString(2,userID);
//                            ps.setString(3,receiverId);
//                            ps.setString(4,url);
                            ps.setInt(5,response.isSuccessful() == true?0:1);
                            ps.setString(6,dateTimeFormatter.format(LocalDateTime.now()));
                        }
                    });
                }
            });
        }
            return ResultMap.ok(CodeMsg.SUCCESS).toString();
        } catch (Exception e){
            e.printStackTrace();
            throw new ZtgeoBizZuulException(CodeMsg.FAIL, "内部异常");
        }

    }

}
