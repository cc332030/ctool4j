package com.c332030.ctool4j.log.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CCommUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.util.CPatternUtils;
import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.enums.CRequestLogTypeEnum;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 * Description: RequestLoggerUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/6
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CRequestLogUtils {

    public final String REQUEST_LOG_STR = "request-log";

    public final String REQUEST_BODY = "requestBody";

    final CLog REQUEST_LOG = CLogUtils.getLog(REQUEST_LOG_STR);

    final ThreadLocal<CRequestLog> REQUEST_LOG_THREAD_LOCAL = new ThreadLocal<>();

    final BlockingQueue<CRequestLog> REQUEST_LOG_QUEUE = new LinkedBlockingQueue<>();

    final Map<String, Object> EMPTY_REQS = getRequestBodyMap("[no request body]");

    final Thread REQUEST_LOG_THREAD = new Thread(CRequestLogUtils::logWrite, REQUEST_LOG_STR + "-thread");
    static {
        REQUEST_LOG_THREAD.start();
    }

    @Setter
    @CAutowired
    CRequestLogConfig requestLogConfig;

    public boolean isEnable() {
        val enable = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getEnable);
        return BooleanUtil.isTrue(enable);
    }

    public boolean isAopEnable() {
        return isEnable() && CRequestLogTypeEnum.AOP.equals(requestLogConfig.getType());
    }

    public boolean isAdviceEnable() {
        return isEnable() && CRequestLogTypeEnum.ADVICE.equals(requestLogConfig.getType());
    }

    public boolean isInterceptorEnable() {
        return isEnable() && CRequestLogTypeEnum.INTERCEPTOR.equals(requestLogConfig.getType());
    }

    public boolean isExcludeUri(String uri) {
        val excludeUriPatterns = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getExcludeUriPatterns);
        if (CollUtil.isEmpty(excludeUriPatterns)) {
            return false;
        }
        return excludeUriPatterns.stream()
            .anyMatch(pattern -> matchUri(uri, pattern));
    }

    private boolean matchUri(String uri, String pattern) {

        if (StrUtil.isEmpty(uri) || StrUtil.isEmpty(pattern)) {
            return false;
        }

        // 1. 如果 pattern 不含通配符，直接等值比较（最快路径）
        if (!pattern.contains("*")) {
            return uri.equals(pattern);
        }

        // 2. 从缓存获取编译好的 Pattern
        val regexPattern = CPatternUtils.getUrlCache(pattern);

        // 3. 使用预编译的 Pattern 进行匹配
        return regexPattern.matcher(uri).matches();
    }

    public Opt<CRequestLog> getOpt() {
        return Opt.ofNullable(REQUEST_LOG_THREAD_LOCAL.get());
    }

    public Opt<CRequestLog> getOptThenRemove() {

        val requestLogOpt = getOpt();
        requestLogOpt.ifPresent(e -> remove());
        return requestLogOpt;
    }

    public CRequestLog genRequestLog() {

        val request = CRequestUtils.getRequest();
        val uri = request.getRequestURI();
        if (isExcludeUri(uri)) {
            log.debug("genRequestLog skip because uri is exclude, uri: {}", uri);
            return null;
        }
        val traceId = CTraceUtils.getTraceId();

        return CRequestLog.builder()
            .traceId(traceId)
            .method(request.getMethod())
            .path(request.getRequestURI())
            .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
            .params(request.getParameterMap())
            .reqs(EMPTY_REQS)
            .ip(CRequestUtils.getIp(request))
            .beginTimeMillis(System.currentTimeMillis())
            .build();
    }

    public void init() {

        val requestLog = genRequestLog();
        if (null != requestLog) {
            REQUEST_LOG_THREAD_LOCAL.set(requestLog);
        }

    }

    public void remove() {
        REQUEST_LOG_THREAD_LOCAL.remove();
    }

    public Map<String, Object> getRequestBodyMap(Object requestBody) {
        return CMap.of(
            REQUEST_BODY, requestBody
        );
    }

    public void setRequestBodyReq(Object req) {
        setPrintAbleReqs(getRequestBodyMap(req));
    }

    public void setPrintAbleReqs(Map<String, Object> reqs) {

        val reqMap = CMapUtils.mapValue(
            reqs,
            CLogUtils::getPrintAble
        );
        setReqs(reqMap);
    }

    public void setReqs(Map<String, Object> reqs) {
        val requestLogOpt = getOpt();
        requestLogOpt
            .ifPresent(requestLog -> requestLog.setReqs(reqs));
    }

    public void write(Object rsp, Throwable throwable) {

        val requestLogOpt = getOpt();
        if (!requestLogOpt.isPresent()) {
            log.debug("write failure because requestLog is null");
            return;
        }
        val requestLog = requestLogOpt.get();

        val endTimeMillis = System.currentTimeMillis();

        requestLog.setEndTimeMillis(endTimeMillis);
        requestLog.setRt(endTimeMillis - requestLog.getBeginTimeMillis());
        requestLog.setRsp(rsp);
        if (null != throwable) {
            requestLog.setThrowableMessage(throwable.getMessage());
        }

        val offerResult = REQUEST_LOG_QUEUE.offer(requestLog);
        if (!offerResult) {
            log.error("REQUEST_LOG_QUEUE offer error, requestLog: {}", requestLog);
        }

    }

    public void logWrite() {

        while (true) {
            try {

                val requestLog = REQUEST_LOG_QUEUE.take();
                logWriteText(requestLog);
            } catch (Throwable e) {
                log.error("logWrite error", e);
            }
        }

    }

    /**
     * 原 JSON 格式输出
     */
    void logWriteJson(CRequestLog requestLog) {
        REQUEST_LOG.infoNonNull("{}", requestLog);
    }

    /**
     * 文本格式输出，参考 CFeignLogger 的 StringBuilder 拼接方式，
     * 输出类似 HTTP 请求+响应的完整 dump，方便调试和回放
     */
    void logWriteText(CRequestLog requestLog) {

        val sb = new StringBuilder();

        // 请求行：METHOD path[?params]（仅 GET 拼接查询参数）
        CCommUtils.appendRequestUrl(sb, requestLog.getMethod(), requestLog.getPath(), requestLog.getParams());

        // 请求头
        CCommUtils.appendHeaderLine(sb, HttpHeaders.AUTHORIZATION, requestLog.getToken());
        CCommUtils.appendHeaderLine(sb, CRequestHeaderEnum.X_REAL_IP, requestLog.getIp());
        CCommUtils.appendHeaderLine(sb, CRequestHeaderEnum.X_TRACE_ID, requestLog.getTraceId());
        CCommUtils.appendHeaderLine(sb, CRequestHeaderEnum.X_TENANT_ID, requestLog.getTenantId());
        CCommUtils.appendHeaderLine(sb, CRequestHeaderEnum.X_USER_ID, requestLog.getUserId());

        // 请求体
        CCommUtils.appendBody(sb, requestLog.getReqs(), "request");

        // 响应体
        val rsp = requestLog.getRsp();
        if (null != rsp) {
            CCommUtils.appendBody(sb, CLogUtils.getPrintAble(rsp), "response");
        }

        val throwableMessage = requestLog.getThrowableMessage();
        if (StrUtil.isNotEmpty(throwableMessage)) {
            sb.append("\n\nerror: ");
            sb.append(throwableMessage);
        }

        // 耗时与异常
        sb.append("\n\n");
        sb.append("rt: ");
        sb.append(requestLog.getRt());
        sb.append("ms");

        REQUEST_LOG.info("{}", sb);
    }

}
