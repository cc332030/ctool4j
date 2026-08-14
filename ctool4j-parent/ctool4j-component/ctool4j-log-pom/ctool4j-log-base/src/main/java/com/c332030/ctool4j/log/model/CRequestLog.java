package com.c332030.ctool4j.log.model;

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

    String method;
    String path;
    String token;

    String traceId;
    String tenantId;
    String userId;

    String ip;

    Map<String, Collection<String>> params;
    Map<String, Object> reqs;
    Object rsp;

    /**
     * 异常信息
     */
    String errorMessage;

    long beginTimeMillis;
    long endTimeMillis;

    /**
     * 完整请求头（headerName → 一个或多个 headerValue），
     * 供 feign 等客户端请求日志使用，仅包含真实请求头；token/traceId 等应用业务数据见 CRequestLogUtils.getBusinessData
     */
    Map<String, Collection<String>> headers;

    /**
     * 完整请求体（已转换为可打印文本），供 feign 等客户端请求日志使用
     */
    Object requestBody;

}
