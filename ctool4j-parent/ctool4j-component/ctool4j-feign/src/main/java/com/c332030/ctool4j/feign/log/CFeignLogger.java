package com.c332030.ctool4j.feign.log;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCommUtils;
import com.c332030.ctool4j.core.util.CThreadLocalUtils;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.net.URL;
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

    static final String FEIGN_LOG_STR = "feign-log";

    static final CLog FEIGN_LOG = CLogUtils.getLog(FEIGN_LOG_STR);

    static final ThreadLocal<Long> START_MILLS = new ThreadLocal<>();

    CFeignClientLogConfig config;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if(CBoolUtils.isTrue(config.getEnable())
            && BooleanUtil.isTrue(config.getEnableCost())
        ) {
            START_MILLS.set(System.currentTimeMillis());
        }
        super.logRequest(configKey, logLevel, request);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {

        if(CBoolUtils.isTrue(config.getEnable())) {
            try {
                if(enableLog(response)) {
                    return dealLog(response);
                }
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            } finally {
                if(BooleanUtil.isTrue(config.getEnableCost())) {
                    val startMills = CThreadLocalUtils.getThenRemove(START_MILLS);
                    if(null != startMills) {
                        val cost = System.currentTimeMillis() - startMills;
                        log.info("cost: {}, elapsedTime: {}", cost, elapsedTime);
                    }
                }
            }
        }

        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {

    }

    @SneakyThrows
    private boolean enableLog(Response response) {

        val request = response.request();
        val url = new URL(request.url());
        val host = url.getHost();
        val path = url.getPath();

        if(config.getHostWhiteList().contains(host)
            || config.getPathWhiteList().contains(path)
        ) {
            return true;
        }

        val apiType = CFeignUtils.getApiType(request.requestTemplate());
        val api = apiType.getSimpleName();
        if(config.getApiWhiteList().contains(api)) {
            return true;
        }

        if(config.getHostBlackList().contains(host)
            || config.getPathBlackList().contains(path)
            || config.getApiBlackList().contains(api)
        ) {
            return false;
        }

        return CBoolUtils.isTrue(config.getLogAll());
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
        FEIGN_LOG.info("{}", httpLog);

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

        if(BooleanUtil.isTrue(config.getEnableHeader())) {

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
