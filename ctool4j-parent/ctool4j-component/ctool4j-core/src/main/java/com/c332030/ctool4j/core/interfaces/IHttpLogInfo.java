package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CMap;

import java.util.Map;

/**
 * <p>
 * Description: HTTP 日志信息接口，各模块实现此接口即可复用 CCommUtils 的日志拼接方法
 * </p>
 *
 * @since 2026/7/3
 */
public interface IHttpLogInfo {

    /** HTTP 方法：GET/POST/PUT/DELETE... */
    String getMethod();

    /** 请求路径（不含 query string） */
    String getPath();

    /** query 参数（仅 GET 时拼接到 URL） */
    default Map<String, String[]> getParams() {
        return CMap.of();
    }

    /** 需要输出的请求头：headerName → headerValue */
    default Map<String, String> getHeaders() {
        return CMap.of();
    }

    /** 请求体 */
    default Object getRequestBody() {
        return null;
    }

    /** 响应体（已经过可打印处理） */
    default Object getResponseBody() {
        return null;
    }

    /** 耗时（毫秒） */
    default Long getRt() {
        return null;
    }

    /** 异常信息 */
    default String getErrorMessage() {
        return null;
    }

}
