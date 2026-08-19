package com.c332030.ctool4j.web.model;

import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CRequestLog
 * </p>
 *
 * @author c332030
 * @since 2024/5/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CRequestLog implements IHttpLogInfo {

    /**
     * HTTP 方法：GET/POST/PUT/DELETE...
     */
    String method;

    /**
     * 请求路径（不含 query string）
     */
    String path;

    /**
     * 认证令牌（Authorization 请求头的值），仅用于日志末尾业务数据区展示
     */
    String token;

    /**
     * 链路追踪 ID
     */
    String traceId;

    /**
     * 租户 ID
     */
    String tenantId;

    /**
     * 用户 ID
     */
    String userId;

    /**
     * 请求来源 IP（仅用于日志展示的元信息，非 HTTP 请求头）
     */
    String ip;

    /**
     * 完整请求头（headerName → 一个或多个 headerValue），
     * 供 feign 等客户端请求日志使用，仅包含真实请求头；token/traceId 等应用业务数据见 CCommUtils.appendHttpLog
     */
    Map<String, Collection<String>> headers;

    /**
     * query 参数（仅 GET 时拼接到 URL）
     */
    Map<String, Collection<String>> params;

    /**
     * 请求体/请求参数（key → value）：服务端 MVC 经 CLogRequestBodyAdvice 记录，feign 客户端经 getRequestBodyMap 记录，
     * 拼接时统一从 reqs 取请求体
     */
    Object reqs;

    /**
     * 响应体：拼接时经 getPrintAble 可打印处理后输出（见 CCommUtils.appendHttpLog）
     */
    Object rsp;

    /**
     * 异常信息
     */
    String errorMessage;

    /**
     * 请求开始时间（毫秒时间戳），用于计算耗时
     */
    long beginTimeMillis;

    /**
     * 请求结束时间（毫秒时间戳），用于计算耗时
     */
    long endTimeMillis;

}
