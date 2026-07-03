package com.c332030.ctool4j.log.model;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.enums.CCommHeaderEnum;
import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import com.c332030.ctool4j.core.log.CLogUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;

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

    Map<String, String[]> params;
    Map<String, Object> reqs;
    Object rsp;

    String throwableMessage;

    long beginTimeMillis;
    long endTimeMillis;
    long rt;

    @Override
    public Map<String, String> getHeaders() {
        val headers = new LinkedHashMap<String, String>();
        if (StrUtil.isNotEmpty(token)) {
            headers.put("Authorization", token);
        }
        if (StrUtil.isNotEmpty(ip)) {
            headers.put(CCommHeaderEnum.X_REAL_IP.getHeaderName(), ip);
        }
        if (StrUtil.isNotEmpty(traceId)) {
            headers.put(CCommHeaderEnum.X_TRACE_ID.getHeaderName(), traceId);
        }
        if (StrUtil.isNotEmpty(tenantId)) {
            headers.put(CCommHeaderEnum.X_TENANT_ID.getHeaderName(), tenantId);
        }
        if (StrUtil.isNotEmpty(userId)) {
            headers.put(CCommHeaderEnum.X_USER_ID.getHeaderName(), userId);
        }
        return headers;
    }

    @Override
    public Object getRequestBody() {
        return reqs;
    }

    @Override
    public Object getResponseBody() {
        return CLogUtils.getPrintAble(rsp);
    }

    @Override
    public Long getRt() {
        return rt;
    }

    @Override
    public String getErrorMessage() {
        return throwableMessage;
    }

}
