package com.c332030.ctool.feign.client;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool.core.util.CCommUtils;
import com.c332030.ctool.core.util.CThreadLocalUtils;
import com.c332030.ctool.feign.config.CFeignLogConfig;
import com.c332030.ctool.feign.util.CFeignUtils;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CFeignClient
 * </p>
 *
 * @since 2025/9/21
 */
@CustomLog
@AllArgsConstructor
public class CFeignClient implements Client {

    Client defaultClient;

    CFeignLogConfig feignLogConfig;

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        if(!BooleanUtil.isTrue(feignLogConfig.getEnable())) {
            return defaultClient.execute(request, options);
        }

        // 执行原始请求，获取响应
        try(val response = defaultClient.execute(request, options)) {

            val headers = response.headers();
            val bodyBytes = Util.toByteArray(response.body().asInputStream());

            try {
                dealLog(headers, bodyBytes);
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            }

            // 重新构建响应体（因为原流已被读取）
            return Response.builder()
                    .requestTemplate(request.requestTemplate())
                    .protocolVersion(response.protocolVersion())
                    .status(response.status())
                    .reason(response.reason())
                    .request(request)
                    .headers(headers)
                    .body(bodyBytes)
                    .build();
        }
    }

    private void dealLog(Map<String, Collection<String>> headers, byte[] bodyBytes) {

        val httpLog = CThreadLocalUtils.getThenRemove(CFeignUtils.HTTP_LOG_THREAD_LOCAL);

        if(BooleanUtil.isTrue(feignLogConfig.getEnableHeader())) {
            httpLog.append("\n\n");
            httpLog.append(CCommUtils.getFullHeaderStr(headers));
        }

        httpLog.append("\n\n");
        if(ArrayUtil.isEmpty(bodyBytes)) {

            httpLog.append("[no response body]");
        } else {

            if(CCommUtils.isTextBody(headers)) {

                val responseBody = new String(bodyBytes, StandardCharsets.UTF_8);
                httpLog.append(responseBody);
            } else {
                httpLog.append("[no response text body]");
            }

        }

    }


}
