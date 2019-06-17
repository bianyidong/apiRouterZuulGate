package com.ztgeo.suqian.common;
import com.alibaba.fastjson.JSONObject;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;

@Component
@Order(1)
public class initUserKeys implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(initUserKeys.class);

    @Autowired
    private RedisOperator redis;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化密钥信息
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {

        log.info("=========密钥初始化,数据加载到Redis,时间:{}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        // 查询数据库里的黑名单列表数据
        List<UserKey> listUserKeys = jdbcTemplate.query("select uki.user_real_id, uki.symmetric_pubkey,uki.sign_secret_key,uki.sign_pub_key,uki.sign_pt_secret_key,uki.sign_pt_pub_key  FROM user_key_info uki ", new BeanPropertyRowMapper<>(UserKey.class));
        for (int i = 0; i < listUserKeys.size(); i++) {
            JSONObject setjsonObject = new JSONObject();
            setjsonObject.put("Symmetric_pubkey", listUserKeys.get(i).getSymmetric_pubkey());
            setjsonObject.put("Sign_secret_key", listUserKeys.get(i).getSign_secret_key());
            setjsonObject.put("Sign_pub_key", listUserKeys.get(i).getSign_pub_key());
            setjsonObject.put("Sign_pt_secret_key", listUserKeys.get(i).getSign_pt_secret_key());
            setjsonObject.put("Sign_pt_pub_key", listUserKeys.get(i).getSign_pt_pub_key());
            //存入Redis
            redis.set(USER_REDIS_SESSION +":"+listUserKeys.get(i).getUser_real_id(), setjsonObject.toJSONString());

        }
    }
}
