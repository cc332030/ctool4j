package com.c332030.ctool4j.web.cors;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.web.advice.handler.ICResponseBeforeBodyWriteHandler;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CorsHandler
 * </p>
 *
 * @since 2025/11/12
 */
@CustomLog
@Component
@AllArgsConstructor
@ConditionalOnBean(CorsConfig.class)
public class CorsHandler implements ICResponseBeforeBodyWriteHandler {

    CorsConfig config;

    @Override
    public Object applyThrowable(HttpServletRequest request, HttpServletResponse response, Object o) {

        handle(request, response);
        return o;
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) {

        val origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.isEmpty(origin)) {
            log.debug("Not cors");
            return;
        }

        val newOrigin = CUrlUtils.getHostWithPort(origin);
        // 同源，避免打印日志误导
        if(Objects.equals(request.getHeader(HttpHeaders.HOST), newOrigin)) {
            return;
        }

        if (!config.getAllowedOrigins().contains(newOrigin)) {
            log.debug("Not allow origin: {}", origin);
            return;
        }

        val allowedMethods = config.getAllowedMethods();
        val method = request.getMethod();
        if (!CCollUtils.containsAny(allowedMethods, CorsConfig.ALL, method)) {
            log.info("Not allow origin with method: {} {}", method, origin);
            return;
        }

        val allowedHeaders = config.getAllowedHeaders();
        val headers = allowedHeaders.contains(CorsConfig.ALL)
                ? CorsConfig.ALL
                : allowedHeaders.stream()
                .filter(header -> StrUtil.isNotEmpty(request.getHeader(header)))
                .collect(Collectors.joining(","));

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, headers);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);

    }

}
