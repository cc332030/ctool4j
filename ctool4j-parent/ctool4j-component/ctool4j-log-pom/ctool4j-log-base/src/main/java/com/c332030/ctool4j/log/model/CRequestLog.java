package com.c332030.ctool4j.log.model;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    String throwableMessage;

    long beginTimeMillis;
    long endTimeMillis;

    /**
     * 完整请求头（headerName → 一个或多个 headerValue），非空时优先返回，
     * 供 feign 等客户端请求日志使用，未配置时由 token/ip/traceId 等字段组合
     */
    Map<String, Collection<String>> headers;

    /**
     * 完整请求体（已转换为可打印文本），非空时优先于 reqs 返回，
     * 供 feign 等客户端请求日志使用，未配置时返回 reqs
     */
    Object requestBody;

    @Override
    public Map<String, Collection<String>> getHeaders() {
        if (MapUtil.isNotEmpty(headers)) {
            return headers;
        }
        val headerMap = new LinkedHashMap<String, Collection<String>>();
        if (StrUtil.isNotEmpty(token)) {
            headerMap.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(token));
        }
        if (StrUtil.isNotEmpty(traceId)) {
            headerMap.put(CRequestHeaderEnum.X_TRACE_ID.getHeaderName(), Collections.singletonList(traceId));
        }
        if (StrUtil.isNotEmpty(tenantId)) {
            headerMap.put(CRequestHeaderEnum.X_TENANT_ID.getHeaderName(), Collections.singletonList(tenantId));
        }
        if (StrUtil.isNotEmpty(userId)) {
            headerMap.put(CRequestHeaderEnum.X_USER_ID.getHeaderName(), Collections.singletonList(userId));
        }
        return headerMap;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public Object getRequestBody() {
        if (null != requestBody) {
            return requestBody;
        }
        return reqs;
    }

    /**
     * 响应体（已可打印处理）：未设置时返回 null，由拼接层输出无响应体占位符
     *
     * @return 响应体，未设置时返回 null
     */
    @Override
    public Object getResponseBody() {
        if (null == rsp) {
            return null;
        }
        return CLogUtils.getPrintAble(rsp);
    }

    /**
     * 耗时（毫秒）：由起止时间计算；未设置或不可用（end 早于 begin）时返回 null，
     * 拼接层不输出耗时，避免展示 rt: 0ms 误导
     *
     * @return 耗时（毫秒），无法计算时返回 null
     */
    @Override
    public Long getRt() {
        if (beginTimeMillis > 0 && endTimeMillis >= beginTimeMillis) {
            return endTimeMillis - beginTimeMillis;
        }
        return null;
    }

    @Override
    public String getErrorMessage() {
        return throwableMessage;
    }

}
