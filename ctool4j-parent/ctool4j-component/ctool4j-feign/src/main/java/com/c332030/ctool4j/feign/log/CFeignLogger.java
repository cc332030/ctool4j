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
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.model.CFeignRequestLog;
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

    static final ThreadLocal<CFeignRequestLog> REQUEST_LOG_THREAD_LOCAL =
        ThreadLocal.withInitial(CFeignRequestLog::new);

    CFeignClientLogConfig config;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if(CBoolUtils.isTrue(config.getEnable())) {
            val requestLog = REQUEST_LOG_THREAD_LOCAL.get();
            if(BooleanUtil.isTrue(config.getEnableCost())) {
                requestLog.setStartMills(System.currentTimeMillis());
            }
            if(enableLog(request)) {
                val httpLog = getRequestLog(request);
                requestLog.setHttpLog(httpLog);
            }
        }
        super.logRequest(configKey, logLevel, request);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
        return dealResponse(response, elapsedTime, this::dealResponseLog);
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        return dealResponse(ioe, elapsedTime, this::dealErrorLog);
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.warn("Don't call this log method");
    }

    @SneakyThrows
    private boolean enableLog(Request request) {

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

    private StringBuilder getRequestLog(Request request) {

        val method = request.httpMethod();
        val url = request.url();

        val httpLog = new StringBuilder();

        httpLog.append("\n");
        httpLog.append(method);
        httpLog.append(" ");
        httpLog.append(url);

        val requestHeaders = request.headers();
        printHeaders(httpLog, requestHeaders, true);
        printBody(httpLog, requestHeaders, request.body(), "request");

        return httpLog;
    }

    private <T> T dealResponse(T t, long elapsedTime, CBiFunction<T, StringBuilder, T> function) {

        if(CBoolUtils.isTrue(config.getEnable())) {
            val requestLog = CThreadLocalUtils.getThenRemove(REQUEST_LOG_THREAD_LOCAL);
            try {
                val httpLog = requestLog.getHttpLog();
                if(null != httpLog) {
                    return function.apply(t, httpLog);
                }
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            } finally {
                val startMills = requestLog.getStartMills();
                if(null != startMills) {
                    val cost = System.currentTimeMillis() - startMills;
                    log.info("cost: {}, elapsedTime: {}", cost, elapsedTime);
                }
            }
        }

        return t;
    }

    private Response dealResponseLog(Response response, StringBuilder httpLog) {

        val responseHeaders = response.headers();
        val responseBodyBytes = getBodyBytes(response);

        printHeaders(httpLog, responseHeaders, false);
        printBody(httpLog, responseHeaders, responseBodyBytes, "response");
        FEIGN_LOG.info("{}", httpLog);

        return CFeignUtils.newResponse(response, responseBodyBytes);
    }

    private IOException dealErrorLog(IOException ioException, StringBuilder httpLog) {

        httpLog.append("\n\n");
        httpLog.append(ioException.getMessage());
        FEIGN_LOG.info("{}", httpLog);

        return ioException;
    }

    private byte[] getBodyBytes(Response response) {
        try {
            val inputStream = CObjUtils.convert(response.body(), Response.Body::asInputStream);
            return CObjUtils.convert(inputStream, Util::toByteArray);
        } catch (Exception e) {
            log.debug("获取响应体失败", e);
            return null;
        }
    }

    private void printHeaders(
        StringBuilder httpLog,
        Map<String, Collection<String>> headers,
        boolean isRequest
    ) {

        if(BooleanUtil.isTrue(config.getEnableHeader())) {

            val headerStr = CCommUtils.getFullHeaderStr(headers);
            if(StrUtil.isNotEmpty(headerStr)) {
                if(!isRequest) {
                    httpLog.append("\n");
                }
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
