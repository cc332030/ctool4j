package com.c332030.ctool4j.web.cors;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCorsHandler
 * </p>
 *
 * @since 2025/11/12
 */
@CustomLog
@Component
@AllArgsConstructor
@ConditionalOnBean(CCorsConfig.class)
public class CCorsHandler implements ICBaseResponseBodyAdvice<Object> {

    CCorsConfig config;

    @Override
    public Object beforeBodyWrite(
        Object body,
        @NonNull MethodParameter returnType,
        @NonNull MediaType selectedContentType,
        @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response
    ) {
        handle(request, response);
        return body;
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

    }

}
