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
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
        // 按开关采集完整请求头（默认关闭，避免日志泄露敏感头），先在 builder 外计算，避免 builder 设置值处出现复杂逻辑
        val headers = CObjUtils.ifThenGet(requestLogConfig.getEnableHeader(), () -> collectHeaders(request));
        return CRequestLog.builder()
            .traceId(traceId)
            .method(request.getMethod())
            .path(request.getRequestURI())
            .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
            .headers(headers)
            // Servlet 的 getParameterMap 返回 Map<String, String[]>，统一转换为集合类型
            .params(CMapUtils.mapValue(request.getParameterMap(), Arrays::asList))
            .reqs(EMPTY_REQS)
            .ip(CRequestUtils.getIp(request))
            .beginTimeMillis(System.currentTimeMillis())
            .build();
    }

    /**
     * 采集全部请求头：Servlet 的请求头视图遍历拷贝为独立 Map，避免日志模型持有请求对象的内部结构
     *
     * @param request HTTP 请求
     * @return 请求头 map（headerName → 值列表），无请求头时返回 null
     */
    private Map<String, Collection<String>> collectHeaders(HttpServletRequest request) {
        val headerNames = request.getHeaderNames();
        if (null == headerNames) {
            return null;
        }
        val headerMap = new LinkedHashMap<String, Collection<String>>();
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement();
            val headerValues = request.getHeaders(headerName);
            val values = new ArrayList<String>();
            while (headerValues.hasMoreElements()) {
                values.add(headerValues.nextElement());
            }
            headerMap.put(headerName, values);
        }
        return headerMap;
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
            requestLog.setErrorMessage(throwable.getMessage());
        }

        logWrite(requestLog);

    }

    /**
     * 请求体：完整请求体非空时优先，否则返回 reqs（服务端 MVC 场景请求体记录在 reqs）
     *
     * @param requestLog 请求日志
     * @return 请求体
     */
    public Object getRequestBody(CRequestLog requestLog) {
        if (null != requestLog.getRequestBody()) {
            return requestLog.getRequestBody();
        }
        return requestLog.getReqs();
    }

    /**
     * 响应体（已可打印处理）：未设置时返回 null，由拼接层输出无响应体占位符
     *
     * @param requestLog 请求日志
     * @return 响应体，未设置时返回 null
     */
    public Object getResponseBody(CRequestLog requestLog) {
        if (null == requestLog.getRsp()) {
            return null;
        }
        return CLogUtils.getPrintAble(requestLog.getRsp());
    }

    /**
     * 耗时（毫秒）：由起止时间计算；未设置或不可用（end 早于 begin）时返回 null
     * <p>包装类型返回 null 表示未测量；业务数据区 {@link #getBusinessData(CRequestLog)} 按 null 判定不输出 rt，
     * 避免未设置时间的日志输出无意义的 0ms</p>
     *
     * @param requestLog 请求日志
     * @return 耗时（毫秒），无法计算时返回 null
     */
    public Long getRt(CRequestLog requestLog) {
        if (requestLog.getBeginTimeMillis() > 0
            && requestLog.getEndTimeMillis() >= requestLog.getBeginTimeMillis()) {
            return requestLog.getEndTimeMillis() - requestLog.getBeginTimeMillis();
        }
        return null;
    }

    /**
     * 应用特定业务数据（非 HTTP 请求头），仅用于日志末尾展示，有值才打印；
     * <p>IP 在耗时前，耗时置于末尾，耗时以 {@link #getRt(CRequestLog)} 是否为空判定：未测量不输出
     * （避免无意义的 rt: 0ms 噪音），真实测量为 0ms 的快速请求仍会输出</p>
     *
     * @param requestLog 请求日志
     * @return 业务数据 map
     */
    public Map<String, String> getBusinessData(CRequestLog requestLog) {

        val businessDataMap = new LinkedHashMap<String, String>();
        CMapUtils.put(businessDataMap, HttpHeaders.AUTHORIZATION, requestLog.getToken());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TRACE_ID.getHeaderName(), requestLog.getTraceId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TENANT_ID.getHeaderName(), requestLog.getTenantId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_USER_ID.getHeaderName(), requestLog.getUserId());
        CMapUtils.put(businessDataMap, "ip", requestLog.getIp());
        val rt = getRt(requestLog);
        if (null != rt) {
            businessDataMap.put("rt", rt + "ms");
        }
        return businessDataMap;
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
        CCommUtils.appendHttpLog(
            sb,
            requestLog,
            getRequestBody(requestLog),
            getResponseBody(requestLog),
            getBusinessData(requestLog)
        );

        // HTTP 报文本身不自带头部换行，logback 输出时以换行开头，使报文从新行开始打印
        REQUEST_LOG.info("\n{}", sb);

    }

}
