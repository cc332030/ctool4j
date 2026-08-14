package com.c332030.ctool4j.log.model;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
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
     * 完整请求头（headerName → 一个或多个 headerValue），
     * 供 feign 等客户端请求日志使用，仅包含真实请求头；token/traceId 等应用业务数据见 {@link #getBusinessData()}
     */
    Map<String, Collection<String>> headers;

    /**
     * 完整请求体（已转换为可打印文本），非空时优先于 reqs 返回，
     * 供 feign 等客户端请求日志使用，未配置时返回 reqs
     */
    Object requestBody;

    @Override
    public Map<String, Collection<String>> getHeaders() {
        return MapUtil.emptyIfNull(headers);
    }

    /**
     * 应用特定业务数据（非 HTTP 请求头），仅用于日志末尾展示，有值才打印；
     * <p>IP 在耗时前，耗时置于末尾，耗时无值时默认 0</p>
     *
     * @return 业务数据 map
     */
    @Override
    public Map<String, String> getBusinessData() {

        val businessDataMap = new LinkedHashMap<String, String>();
        CMapUtils.put(businessDataMap, HttpHeaders.AUTHORIZATION, token);
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TRACE_ID.getHeaderName(), traceId);
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TENANT_ID.getHeaderName(), tenantId);
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_USER_ID.getHeaderName(), userId);
        CMapUtils.put(businessDataMap, "ip", ip);
        businessDataMap.put("rt", getRt() + "ms");
        return businessDataMap;
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
     * 耗时（毫秒）：由起止时间计算；未设置或不可用（end 早于 begin）时返回 0
     * <p>基础类型无法返回 null，业务数据区 {@link #getBusinessData()} 恒输出 rt（含 0ms）</p>
     *
     * @return 耗时（毫秒），无法计算时返回 0
     */
    @Override
    public long getRt() {
        if (beginTimeMillis > 0 && endTimeMillis >= beginTimeMillis) {
            return endTimeMillis - beginTimeMillis;
        }
        return 0;
    }

    @Override
    public String getErrorMessage() {
        return throwableMessage;
    }

}
