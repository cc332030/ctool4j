package com.c332030.ctool4j.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.enums.CLogSource;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.util.CPatternUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.config.CRequestLogConfig;
import com.c332030.ctool4j.web.model.CRequestLog;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <p>
 * Description: CRequestLogUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/6
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CRequestLogUtils {

    /**
     * 请求日志 logger 名称
     */
    public final String REQUEST_LOG_STR = "request-log";

    final CLog REQUEST_LOG = CLogUtils.getLog(REQUEST_LOG_STR);

    final ThreadLocal<CRequestLog> REQUEST_LOG_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 无请求体时的占位字符串，所有请求共享；req 统一为 Object，直接存字符串与 feign 场景语义一致
     */
    public final String EMPTY_REQ = "[no request body]";

    /**
     * 无响应体时的占位字符串，服务端 MVC 请求日志初始化为该值，实际响应有值时由 setRsp 覆盖
     */
    public final String EMPTY_RSP = "[no response body]";

    @Setter
    @CAutowired
    CRequestLogConfig requestLogConfig;

    /**
     * 判断请求日志功能是否开启
     *
     * @return true 表示开启
     */
    public boolean isEnable() {
        val enable = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getEnable);
        return BooleanUtil.isTrue(enable);
    }

    /**
     * 判断 uri 是否命中排除规则
     *
     * @param uri 请求 uri
     * @return true 表示命中排除规则
     */
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

    /**
     * 获取当前线程的请求日志
     *
     * @return 当前线程的请求日志，无则返回空 Opt
     */
    public Opt<CRequestLog> getOpt() {
        return Opt.ofNullable(REQUEST_LOG_THREAD_LOCAL.get());
    }

    /**
     * 获取当前线程的请求日志并移除
     *
     * @return 当前线程的请求日志，无则返回空 Opt
     */
    public Opt<CRequestLog> getOptThenRemove() {

        val requestLogOpt = getOpt();
        requestLogOpt.ifPresent(e -> remove());
        return requestLogOpt;
    }

    /**
     * 根据当前请求生成请求日志
     *
     * @return 生成的请求日志；uri 命中排除规则时返回 null
     */
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
            .source(CLogSource.MVC)
            .method(request.getMethod())
            .path(request.getRequestURI())
            .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
            // 总是采集请求头存储，是否输出由打印层 enableHeader 开关控制
            .requestHeaders(collectHeaders(request))
            // Servlet 的 getParameterMap 返回 Map<String, String[]>，统一转换为集合类型
            .params(CMapUtils.mapValue(request.getParameterMap(), Arrays::asList))
            .req(EMPTY_REQ)
            .rsp(EMPTY_RSP)
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

    /**
     * 初始化请求日志并绑定到当前线程
     */
    public void init() {

        val requestLog = genRequestLog();
        if (null != requestLog) {
            REQUEST_LOG_THREAD_LOCAL.set(requestLog);
        }

    }

    /**
     * 移除当前线程绑定的请求日志
     */
    public void remove() {
        REQUEST_LOG_THREAD_LOCAL.remove();
    }

    /**
     * 设置请求体到请求日志
     *
     * @param req 请求体
     */
    public void setRequestBodyReq(Object req) {
        setPrintAbleReq(req);
    }

    /**
     * 将可打印的请求参数设置到请求日志
     *
     * @param req 请求体
     */
    public void setPrintAbleReq(Object req) {
        setReq(CLogUtils.getPrintAble(req));
    }

    /**
     * 将请求参数设置到请求日志
     *
     * @param req 请求体
     */
    public void setReq(Object req) {
        val requestLogOpt = getOpt();
        requestLogOpt
            .ifPresent(requestLog -> requestLog.setReq(req));
    }

    /**
     * 记录响应侧信息到请求日志：响应体、异常信息、响应状态码与响应头（只记录不打印，
     * 打印由拦截器 afterCompletion 统一出口 logWrite 执行）。
     * <p>一次取回 requestLog 统一设置，避免多次 getOpt 重复开销</p>
     *
     * @param rsp       响应对象，无则传 null
     * @param throwable 异常，无则传 null
     * @param response  HTTP 响应（用于采集响应状态码与响应头），无则传 null
     */
    public void setRsp(Object rsp, Throwable throwable, HttpServletResponse response) {

        val requestLogOpt = getOpt();
        if (!requestLogOpt.isPresent()) {
            log.debug("setRsp failure because requestLog is null");
            return;
        }
        val requestLog = requestLogOpt.get();

        if (null != rsp) {
            requestLog.setRsp(rsp);
        }
        if (null != throwable) {
            requestLog.setErrorMessage(throwable.getMessage());
        }
        if (null != response) {
            requestLog.setResponseStatus(response.getStatus());
            val responseHeaders = collectResponseHeaders(response);
            if (MapUtil.isNotEmpty(responseHeaders)) {
                requestLog.setResponseHeaders(responseHeaders);
            }
        }
    }

    /**
     * 采集全部响应头：Servlet 的响应头视图遍历拷贝为独立 Map，避免日志模型持有响应对象的内部结构
     *
     * @param response HTTP 响应
     * @return 响应头 map（headerName → 值列表），无响应头时返回 null
     */
    private Map<String, Collection<String>> collectResponseHeaders(HttpServletResponse response) {
        val headerNames = response.getHeaderNames();
        if (CollUtil.isEmpty(headerNames)) {
            return null;
        }
        val headerMap = new LinkedHashMap<String, Collection<String>>();
        for (val headerName : headerNames) {
            val headerValues = response.getHeaders(headerName);
            if (CollUtil.isEmpty(headerValues)) {
                continue;
            }
            headerMap.put(headerName, new ArrayList<>(headerValues));
        }
        return headerMap;
    }

    /**
     * 记录请求日志、设置属性、打印日志的统一出口，默认打印 http 格式
     * <p>输出类似 HTTP 请求+响应的完整 dump，方便调试和回放</p>
     * <p>所有请求方式（服务端 MVC、feign、resttemplate、httpclient 等）构造 {@link CRequestLog} 后
     * 均可调用本方法统一打印，后续新增请求方式无需各自实现拼接逻辑</p>
     *
     * @param info         请求日志信息
     * @param enableHeader 是否打印请求头/响应头（打印层开关，采集层总是采集存储）
     */
    public void logWrite(CRequestLog info, boolean enableHeader) {

        val sb = new StringBuilder();
        CCommUtils.appendHttpLog(sb, info, enableHeader);

        // HTTP 报文本身不自带头部换行，logback 输出时以换行开头，使报文从新行开始打印
        REQUEST_LOG.info("\n{}", sb);

    }

}
