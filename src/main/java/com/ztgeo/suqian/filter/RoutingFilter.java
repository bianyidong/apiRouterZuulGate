package com.ztgeo.suqian.filter;

import com.github.wxiaoqi.security.common.util.UUIDUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.ztgeo.suqian.common.GlobalConstants;
import com.ztgeo.suqian.common.ZtgeoBizZuulException;
import com.ztgeo.suqian.entity.ApiBaseInfo;
import com.ztgeo.suqian.msg.CodeMsg;
import com.ztgeo.suqian.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class
RoutingFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(RoutingFilter.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        InputStream inputStream = null;
        try {
            log.info("=================进入routing过滤器,待转发=====================");
            RequestContext ctx = RequestContext.getCurrentContext();
            Object routeHost = ctx.get("routeHost");
            Object requestURI = ctx.get(FilterConstants.REQUEST_URI_KEY);
            if (Objects.equals(null, routeHost) || Objects.equals(null, requestURI))
                throw new ZtgeoBizZuulException(CodeMsg.FAIL, "未匹配到路由规则或请求路径获取失败");
            // 查找base_url和path
            List<ApiBaseInfo> list = jdbcTemplate.query("select * from api_base_info where base_url='" + routeHost + "' and path = '" + requestURI + "'", new BeanPropertyRowMapper<>(ApiBaseInfo.class));
            // 记录
            if (!Objects.equals(null, list) && list.size() != 0) {
                String id = UUIDUtils.generateShortUuid(); // 主键ID
                ApiBaseInfo apiBaseInfo = list.get(0);
                LocalDateTime localDateTime = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String currentTime = dateTimeFormatter.format(localDateTime);
                HttpServletRequest request = ctx.getRequest();
                String accessClientIp = HttpUtils.getIpAdrress(request);
                inputStream = request.getInputStream();
                String requestBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                log.info("匹配到转发规则,待转发URL:{},接口名称:{}", apiBaseInfo.getBaseUrl() + apiBaseInfo.getPath(), apiBaseInfo.getApiName());
                jdbcTemplate.update("insert into api_access_record values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{id, apiBaseInfo.getApiId(), apiBaseInfo.getApiName(), apiBaseInfo.getBaseUrl() + apiBaseInfo.getPath(), 1, accessClientIp, localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), currentTime, requestBody, "", apiBaseInfo.getApiType(), apiBaseInfo.getApiOwnerId(), 0, 0});
                ctx.set(GlobalConstants.RECORD_PRIMARY_KEY, id);
                ctx.set(GlobalConstants.ACCESS_IP_KEY, accessClientIp);
            } else {
                log.info("未匹配到注册路由,请求路径:{}", requestURI);
                throw new ZtgeoBizZuulException(CodeMsg.NOT_FOUND, "未匹配到注册路由,请求路径:" + routeHost + requestURI);
            }
            return null;
        } catch (ZuulException z) {
            throw new ZtgeoBizZuulException(z, z.getMessage(), z.nStatusCode, z.errorCause);
        } catch (Exception s) {
            throw new ZtgeoBizZuulException(s, CodeMsg.FAIL, "routing过滤器内部异常");
        } finally {
            try {
                if (!Objects.equals(null, inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
