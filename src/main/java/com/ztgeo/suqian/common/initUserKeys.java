package com.ztgeo.suqian.common;
import com.alibaba.fastjson.JSONObject;
import com.ztgeo.suqian.config.RedisOperator;
import com.ztgeo.suqian.entity.ag_datashare.UserKeyInfo;
import com.ztgeo.suqian.repository.UserKeyInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ztgeo.suqian.common.GlobalConstants.USER_REDIS_SESSION;

//@Component
@Order(1)
public class initUserKeys implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(initUserKeys.class);
    @Resource
    private UserKeyInfoRepository userKeyInfoRepository;
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
        //查询所有用户密钥信息
        List<UserKeyInfo> listUserKeys=userKeyInfoRepository.findAll();
        for (int i = 0; i < listUserKeys.size(); i++) {
            JSONObject setjsonObject = new JSONObject();
            setjsonObject.put("Symmetric_pubkey", listUserKeys.get(i).getSymmetricPubkey());
            setjsonObject.put("Sign_secret_key", listUserKeys.get(i).getSignSecretKey());
            setjsonObject.put("Sign_pub_key", listUserKeys.get(i).getSignPubKey());
            setjsonObject.put("Sign_pt_secret_key", listUserKeys.get(i).getSignPtSecretKey());
            setjsonObject.put("Sign_pt_pub_key", listUserKeys.get(i).getSignPtPubKey());
            //存入Redis
            redis.set(USER_REDIS_SESSION +":"+listUserKeys.get(i).getUserRealId(), setjsonObject.toJSONString());

        }
    }
}
