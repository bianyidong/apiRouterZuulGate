package com.ztgeo.suqian.rest;

import com.ztgeo.suqian.msg.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定义zuul网关本身的操作
 *
 * @author zoupeidong
 * @version 2018-12-7
 */
@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    RouteLocator routeLocator;

    @GetMapping("refreshRouteList")
    public String refreshRouteList(){
        log.info("======刷新路由列表完成======");
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        applicationEventPublisher.publishEvent(routesRefreshedEvent);
        return ResultMap.ok().toString();
    }

}
