package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CMap;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: IHttpLogInfo 日志信息接口，各模块实现此接口即可复用 CCommUtils 的日志拼接方法
 * </p>
 *
 * @since 2026/7/3
 */
public interface IHttpLogInfo {

    /**
     * HTTP 方法：GET/POST/PUT/DELETE...
     * @return HTTP 方法
     */
    String getMethod();

    /**
     * 请求路径（不含 query string）
     * @return 请求路径（不含 query string）
     */
    String getPath();

    /**
     * 认证令牌（Authorization 请求头的值），仅用于日志末尾业务数据区展示
     * @return 认证令牌
     */
    default String getToken() {
        return null;
    }

    /**
     * 链路追踪 ID
     * @return 链路追踪 ID
     */
    default String getTraceId() {
        return null;
    }

    /**
     * 租户 ID
     * @return 租户 ID
     */
    default String getTenantId() {
        return null;
    }

    /**
     * 用户 ID
     * @return 用户 ID
     */
    default String getUserId() {
        return null;
    }

    /**
     * 请求来源 IP（仅用于日志展示的元信息，非 HTTP 请求头）
     * @return 请求来源 IP
     */
    default String getIp() {
        return null;
    }

    /**
     * 需要输出的请求头：headerName → 一个或多个 headerValue
     * @return 请求头 map
     */
    default Map<String, Collection<String>> getHeaders() {
        return CMap.of();
    }

    /**
     * query 参数（仅 GET 时拼接到 URL）
     * @return query 参数 map
     */
    default Map<String, Collection<String>> getParams() {
        return CMap.of();
    }

    /**
     * 请求体/请求参数（key → value）：服务端 MVC 经 CLogRequestBodyAdvice 记录，feign 客户端经 getRequestBodyMap 记录，
     * 拼接时统一从 reqs 取请求体
     * @return 请求体/请求参数 map
     */
    default Map<String, Object> getReqs() {
        return CMap.of();
    }

    /**
     * 响应体：拼接时经 getPrintAble 可打印处理后输出（见 CCommUtils.appendHttpLog）
     * @return 响应体
     */
    default Object getRsp() {
        return null;
    }

    /**
     * 异常信息
     * @return 异常信息
     */
    default String getErrorMessage() {
        return null;
    }

    /**
     * 请求开始时间（毫秒时间戳），用于计算耗时
     * @return 请求开始时间（毫秒时间戳）
     */
    default long getBeginTimeMillis() {
        return 0;
    }

    /**
     * 请求结束时间（毫秒时间戳），用于计算耗时
     * @return 请求结束时间（毫秒时间戳）
     */
    default long getEndTimeMillis() {
        return 0;
    }

}
