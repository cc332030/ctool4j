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
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 无请求体时的占位 map，所有请求共享；只读不可修改（unmodifiable 保护）
     */
    final Map<String, Object> EMPTY_REQS = Collections.unmodifiableMap(getRequestBodyMap("[no request body]"));

    @Setter
    @CAutowired
    CRequestLogConfig requestLogConfig;

    public boolean isEnable() {
        val enable = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getEnable);
        return BooleanUtil.isTrue(enable);
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
            // Servlet 的 getParameterMap 返回 Map<String, String[]>，统一转换为集合类型
            .params(request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.asList(entry.getValue()))))
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
        requestLog.setRsp(rsp);
        if (null != throwable) {
            requestLog.setThrowableMessage(throwable.getMessage());
        }

        logWrite(requestLog);

    }

    /**
     * 记录请求日志、设置属性、打印日志的统一出口，默认打印 http 格式
     * <p>输出类似 HTTP 请求+响应的完整 dump，方便调试和回放</p>
     * <p>所有请求方式（服务端 MVC、feign、resttemplate、httpclient 等）构造 {@link CRequestLog} 后
     * 均可调用本方法统一打印，后续新增请求方式无需各自实现拼接逻辑</p>
     *
     * @param requestLog 请求日志
     */
    public void logWrite(CRequestLog requestLog) {

        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, requestLog);

        REQUEST_LOG.info("{}", sb);
    }

}
