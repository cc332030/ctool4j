package com.c332030.ctool4j.web.cors.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * Description: CCorsUtils
 * </p>
 *
 * @since 2026/1/9
 * @see "doc/design/web/CCorsUtils.adoc"
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CCorsUtils {

    @Setter
    @CAutowired
    CCorsConfig config;

    /**
     * 处理 OPTIONS 预检请求，直接返回 204
     *
     * <p>预检请求的 CORS 响应头由 {@link #handle} / {@link #handleDo} 在处理链中先行设置，
     * 此处仅负责以 204 状态码结束预检请求，不再重复设置响应头（有意设计）</p>
     *
     * @param request  请求
     * @param response 响应
     * @return true 表示本次为预检请求且已处理
     */
    public boolean handleOptions(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                    log.debug("deal OPTIONS request");
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return true;
                }
            }
        } catch (Throwable e) {
            log.error("deal OPTIONS failure", e);
        }

        return false;
    }

    /**
     * 处理跨域请求，开启跨域时设置响应头
     *
     * @param request  请求
     * @param response 响应
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                handleDo(request, response);
            }
        } catch (Throwable e) {
            log.error("cors handle failure", e);
        }

    }

    /**
     * 按配置校验并设置跨域响应头
     *
     * @param request  请求
     * @param response 响应
     */
    public void handleDo(HttpServletRequest request, HttpServletResponse response) {

        val origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StrUtil.isEmpty(origin)) {
            log.debug("Not cors");
            return;
        }

        val newOrigin = CUrlUtils.getHostWithPort(origin);
        // 同源，避免打印日志误导
        if (Objects.equals(request.getHeader(HttpHeaders.HOST), newOrigin)) {
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

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, config.getAllowedHeaders());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        // 允许当前请求方法类型
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, method);

        // 暴露给浏览器脚本可读的响应头（默认仅简单响应头可读，如 Authorization 需显式暴露）
        setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, config.getExposedHeaders());

    }

    /**
     * 头集合非空时，拼接（含 {@link CCorsConfig#ALL} 用 {@code *}）并设置响应头；集合为 null 或空则均不设置
     *
     * @param response   响应
     * @param headerName 响应头名
     * @param headers    头集合
     */
    public void setHeaderIfNotEmpty(
        HttpServletResponse response,
        String headerName,
        Set<String> headers
    ) {
        if (CollUtil.isEmpty(headers)) {
            return;
        }
        response.setHeader(headerName, joinHeaders(headers));
    }

    /**
     * 将头集合转为逗号分隔的头值；集合含 {@link CCorsConfig#ALL} 时直接使用 {@code *} 通配
     *
     * @param headers 头集合（已保证非 null 非空，由 {@link #setHeaderIfNotEmpty} 调用）
     * @return 头值
     */
    public String joinHeaders(Set<String> headers) {
        return headers.contains(CCorsConfig.ALL)
            ? CCorsConfig.ALL
            : CollUtil.join(headers, ",");
    }

}
