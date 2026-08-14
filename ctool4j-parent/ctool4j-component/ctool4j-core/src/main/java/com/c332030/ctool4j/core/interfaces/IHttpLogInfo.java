package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CMap;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: HTTP 日志信息接口，各模块实现此接口即可复用 CCommUtils 的日志拼接方法
 * </p>
 *
 * @since 2026/7/3
 */
public interface IHttpLogInfo {

    /**
     * HTTP 方法：GET/POST/PUT/DELETE...
     */
    String getMethod();

    /**
     * 请求路径（不含 query string）
     */
    String getPath();

    /**
     * 请求来源 IP（仅用于日志展示的元信息，非 HTTP 请求头）
     */
    default String getIp() {
        return null;
    }

    /**
     * query 参数（仅 GET 时拼接到 URL）
     */
    default Map<String, Collection<String>> getParams() {
        return CMap.of();
    }

    /**
     * 需要输出的请求头：headerName → 一个或多个 headerValue
     */
    default Map<String, Collection<String>> getHeaders() {
        return CMap.of();
    }

    /**
     * 异常信息
     */
    default String getErrorMessage() {
        return null;
    }

}
