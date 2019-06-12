package com.ztgeo.suqian.common;

import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.msg.CodeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义Zuul异常
 *
 * @author zoupeidong
 * @version 2018-12-6
 */
public class ZtgeoBizZuulException extends ZuulException{

    private static Logger log = LoggerFactory.getLogger(ZtgeoBizZuulException.class);

    public ZtgeoBizZuulException(CodeMsg codeMsg) {
        super(codeMsg.message(), codeMsg.statusCode(), "");
    }

    public ZtgeoBizZuulException(CodeMsg codeMsg, String errorCause) {
        super(codeMsg.message(), codeMsg.statusCode(), errorCause);
    }

    public ZtgeoBizZuulException(Throwable throwable,CodeMsg codeMsg, String errorCause) {
        super(throwable, codeMsg.statusCode(), codeMsg.message()+"，"+errorCause);
        log.error("平台内部错误,异常信息:{}",codeMsg.message()+"，"+errorCause,throwable);
    }

    public ZtgeoBizZuulException(Throwable throwable, String sMessage, int nStatusCode, String errorCause) {
        super(throwable, sMessage, nStatusCode, errorCause);
        log.error("平台内部错误,异常信息:{}",sMessage+"，"+errorCause,throwable);
    }

    public ZtgeoBizZuulException(String sMessage, int nStatusCode, String errorCause) {
        super(sMessage, nStatusCode, errorCause);
    }

    public ZtgeoBizZuulException(Throwable throwable, int nStatusCode, String errorCause) {
        super(throwable, nStatusCode, errorCause);
        log.error("平台内部错误,异常信息:{}",errorCause,throwable);
    }
}
