package com.ztgeo.suqian;

import com.ztgeo.suqian.filter.AccessAuthCheckFilter;
import com.ztgeo.suqian.filter.ResponseFilter;
import com.ztgeo.suqian.filter.RoutingFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@EnableZuulProxy
@SpringBootApplication
public class ApiRouterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiRouterApplication.class, args);
	}

	@Bean
	public AccessAuthCheckFilter gateFilter(){
		return new AccessAuthCheckFilter();
	}


	@Bean
	public RoutingFilter routeFilter(){
		return new RoutingFilter();
	}

	@Bean
	public ResponseFilter responseFilter(){
		return new ResponseFilter();
	}

}
