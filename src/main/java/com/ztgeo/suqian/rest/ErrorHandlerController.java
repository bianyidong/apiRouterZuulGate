package com.ztgeo.suqian.rest;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.msg.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
public class ErrorHandlerController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerController.class);

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request) {
        ZuulException zuulException = (ZuulException) RequestContext.getCurrentContext().getThrowable(); // 获取业务异常对象
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code"); // 获取request状态码，用于处理404情况
        if (!Objects.equals(null, zuulException)) { // 捕捉业务错误
            log.error("statusCode:{},message:{},errorCause:{}",zuulException.nStatusCode,zuulException,zuulException.errorCause);
            return ResultMap.error(zuulException.nStatusCode, zuulException.getMessage(), zuulException.errorCause).toString();
        } else if (!Objects.equals(null, statusCode) && Objects.equals(404, statusCode)) { // 处理404
            log.error(CodeMsg.NOT_FOUND.message());
            return ResultMap.error(CodeMsg.NOT_FOUND).toString();
        } else { // 其它所有异常均返回500
            log.error(CodeMsg.FAIL.message());
            return ResultMap.error(CodeMsg.FAIL).toString();
        }
    }

}
