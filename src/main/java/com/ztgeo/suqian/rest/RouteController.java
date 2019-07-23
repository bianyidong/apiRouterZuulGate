package com.ztgeo.suqian.rest;

import com.alibaba.fastjson.JSONObject;
import com.ztgeo.suqian.common.ZtgeoBizRuntimeException;
import com.ztgeo.suqian.entity.ag_datashare.NoticeRecord;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.msg.ResultMap;
import com.ztgeo.suqian.repository.NoticeRecordRepository;
import com.ztgeo.suqian.utils.CommonResponseEntity;
import com.ztgeo.suqian.utils.HttpOperation;
import com.ztgeo.suqian.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * 定义zuul网关本身的操作
 *
 * @author zoupeidong
 * @version 2018-12-7
 */
@RestController
@RequestMapping("/route")
public class RouteController {
    @Resource
    private NoticeRecordRepository noticeRecordRepository;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
   private static final Logger log = LoggerFactory.getLogger(RouteController.class);
    @Autowired
    RouteLocator routeLocator;
    @Scheduled(fixedRate = 200000)
    public String refreshRouteList(){
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        applicationEventPublisher.publishEvent(routesRefreshedEvent);
        return ResultMap.ok().toString();
    }

    // 每天下午17：30执行通知重发
    @Scheduled(cron = "0 30 20 * * ?")
    public void sendNoticeRestart(){
        List<NoticeRecord> listLogs = noticeRecordRepository.findNoticeRecordsByStatusAndCountLessThan(1,3);
        for (int i = 0; i < listLogs.size(); i++) {
            try {
                String rspData = HttpOperation.sendJsonHttp(listLogs.get(i).getReceiverUrl(), listLogs.get(i).getRequestData());
                CommonResponseEntity commonResponseEntity = JSONObject.parseObject(rspData, CommonResponseEntity.class);
                if (StringUtils.isBlank(rspData)) {
                    log.info("未接收到{}响应数据",listLogs.get(i).getReceiverUrl());
                    throw new ZtgeoBizRuntimeException(CodeMsg.FAIL, "未接收到响应数据");
                } else {
                    if (commonResponseEntity.getCode() == 200) {
                        noticeRecordRepository.updateNoticeRecordStatusSuccess(listLogs.get(i).getRecordId());
                    } else {
                        int count = listLogs.get(i).getCount() + 1;
                        noticeRecordRepository.updateNoticeRecordCount(count,listLogs.get(i).getRecordId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                int count = listLogs.get(i).getCount() + 1;
                noticeRecordRepository.updateNoticeRecordCount(count,listLogs.get(i).getRecordId());
            }
        }
    }
}
