package com.c332030.ctool4j.web.cors.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCorsUtils
 * </p>
 *
 * @since 2026/1/9
 */
@CustomLog
@UtilityClass
public class CCorsUtils {

    final ThreadLocal<Boolean> CORS_THREAD_LOCAL = new ThreadLocal<>();

    @CAutowired
    CCorsConfig config;

    public boolean hasHandle() {
        return CBoolUtils.isTrue(CCorsUtils.CORS_THREAD_LOCAL.get());
    }

    public void setHandled() {
        CORS_THREAD_LOCAL.set(true);
    }

    public void clear() {
        CORS_THREAD_LOCAL.remove();
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        try {
            if(null != config && CBoolUtils.isTrue(config.getEnable())) {
                handleDo(request, response);
            }
        } catch (Throwable e) {
            log.error("cors handle failure", e);
        }
    }

    public void handleDo(HttpServletRequest request, HttpServletResponse response) {

        if(hasHandle()) {
            log.debug("cors handled");
            return;
        }

        try {

            val origin = request.getHeader(HttpHeaders.ORIGIN);
            if (StrUtil.isEmpty(origin)) {
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
            if (!CCollUtils.containsAny(allowedMethods, CCorsConfig.ALL, method)) {
                log.info("Not allow origin with method: {} {}", method, origin);
                return;
            }

            val allowedHeaders = config.getAllowedHeaders();
            val headers = allowedHeaders.contains(CCorsConfig.ALL)
                ? CCorsConfig.ALL
                : allowedHeaders.stream()
                .filter(header -> StrUtil.isNotEmpty(request.getHeader(header)))
                .collect(Collectors.joining(","));

            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, headers);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);

        } finally {
            log.debug("finish handle cors");
            setHandled();
        }

    }

}
