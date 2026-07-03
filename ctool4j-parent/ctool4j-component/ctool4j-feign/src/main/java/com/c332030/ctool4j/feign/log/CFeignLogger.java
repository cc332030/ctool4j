package com.c332030.ctool4j.feign.log;

import cn.hutool.core.util.BooleanUtil;
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

        CCommUtils.appendRequestLine(httpLog, method.name(), url);

        val requestHeaders = request.headers();
        if(BooleanUtil.isTrue(config.getEnableHeader())) {
            CCommUtils.appendHeaderBlock(httpLog, requestHeaders);
        }
        CCommUtils.appendBody(httpLog, request.body(), requestHeaders, "request");

        return httpLog;
    }

    private <T> T dealResponse(T t, long elapsedTime, CBiFunction<T, StringBuilder, T> function) {

        if(CBoolUtils.isTrue(config.getEnable())) {
            val requestLog = CThreadLocalUtils.getThenRemove(REQUEST_LOG_THREAD_LOCAL);

            StringBuilder httpLog = null;
            try {
                httpLog = requestLog.getHttpLog();
                if(null != httpLog) {
                    return function.apply(t, httpLog);
                }
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            } finally {
                val startMills = requestLog.getStartMills();
                if(null != startMills && null != httpLog) {
                    val cost = System.currentTimeMillis() - startMills;
                    httpLog.append("\n\n");
                    httpLog.append("cost: ");
                    httpLog.append(cost);
                    httpLog.append(", elapsedTime: ");
                    httpLog.append(elapsedTime);
                }
                if(null != httpLog) {
                    FEIGN_LOG.info("{}", httpLog);
                }
            }
        }

        return t;
    }

    private Response dealResponseLog(Response response, StringBuilder httpLog) {

        val responseHeaders = response.headers();
        val responseBodyBytes = getBodyBytes(response);

        if(BooleanUtil.isTrue(config.getEnableHeader())) {
            CCommUtils.appendHeaderBlock(httpLog, responseHeaders);
        }
        CCommUtils.appendBody(httpLog, responseBodyBytes, responseHeaders, "response");

        return CFeignUtils.newResponse(response, responseBodyBytes);
    }

    private IOException dealErrorLog(IOException ioException, StringBuilder httpLog) {

        httpLog.append("\n\n");
        httpLog.append(ioException.getMessage());

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

}
