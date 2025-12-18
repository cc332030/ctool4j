package com.c332030.ctool4j.feign.log;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CCommUtils;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import feign.Logger;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CFeignLogger
 * </p>
 *
 * @since 2025/12/2
 */
@CustomLog
@AllArgsConstructor
public class CFeignLogger extends Logger {

    CFeignClientLogConfig feignLogConfig;

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {

        if(BooleanUtil.isTrue(feignLogConfig.getEnable())) {
            try {
                return dealLog(response);
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            }
        }

        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {

    }

    @SneakyThrows
    private Response dealLog(Response response) {

        val request = response.request();
        val method = request.httpMethod();
        val url = request.url();

        val httpLog = new StringBuilder();

        httpLog.append("\n");
        httpLog.append(method);
        httpLog.append(" ");
        httpLog.append(url);

        val requestHeaders = request.headers();
        printHeaders(httpLog, requestHeaders);
        printBody(httpLog, requestHeaders, request.body(), "request");

        val responseHeaders = response.headers();
        val responseBodyBytes = getBodyBytes(response);

        httpLog.append("\n");
        printHeaders(httpLog, responseHeaders);
        printBody(httpLog, responseHeaders, responseBodyBytes, "response");

        log.info("{}", httpLog);

        return CFeignUtils.newResponse(response, responseBodyBytes);
    }

    @SneakyThrows
    private byte[] getBodyBytes(Response response) {
        try {
            val inputStream = CObjUtils.convert(response.body(), Response.Body::asInputStream);
            return CObjUtils.convert(inputStream, Util::toByteArray);
        } catch (Exception e) {
            log.debug("获取响应体失败", e);
            return null;
        }
    }

    private void printHeaders(StringBuilder httpLog, Map<String, Collection<String>> headers) {

        if(BooleanUtil.isTrue(feignLogConfig.getEnableHeader())) {

            val headerStr = CCommUtils.getFullHeaderStr(headers);
            if(StrUtil.isNotEmpty(headerStr)) {
                httpLog.append("\n");
                httpLog.append(CCommUtils.getFullHeaderStr(headers));
            }
        }

    }

    private void printBody(
        StringBuilder httpLog,
        Map<String, Collection<String>> headers,
        byte[] bodyBytes,
        String type
    ) {

        httpLog.append("\n\n");
        if(ArrayUtil.isEmpty(bodyBytes)) {

            httpLog.append("[no ");
            httpLog.append(type);
            httpLog.append(" body]");
        } else {

            if(CCommUtils.isTextBody(headers)) {

                val responseBody = new String(bodyBytes, StandardCharsets.UTF_8);
                httpLog.append(responseBody);
            } else {
                httpLog.append("[not ");
                httpLog.append(type);
                httpLog.append(" text body]");
            }

        }

    }

}
