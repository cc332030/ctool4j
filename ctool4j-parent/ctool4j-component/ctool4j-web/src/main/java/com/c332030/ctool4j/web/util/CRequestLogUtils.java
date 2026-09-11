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
 * <h2>能力目录</h2>
 * <p>{@code CRequestLogUtils}（{@code @UtilityClass} + {@code @CAutowiredScan} + {@code @CustomLog}）为请求日志采集/打印的静态工具类， 持有 {@code @CAutowired} 注入的 {@code CRequestLogConfig requestLogConfig}，并以 {@code ThreadLocal&lt;CRequestLog&gt;} 保存当前线程请求日志。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>内置静态资源默认路径（{@code STATIC_RESOURCE_EXCLUDE_PATTERNS}）无需配置即排除，与用户配置 {@code exclude-uri-patterns} 叠加，命中任一即排除</li>
 *   <li>{@code getOpt()} / {@code getOptThenRemove()}：获取当前线程请求日志（Opt），后者取出并移除</li>
 *   <li>{@code collectHeaders}：采集全部请求头（拷贝为独立 Map，避免持有请求对象内部结构）</li>
 *   <li>{@code setRequestBodyReq(req)} / {@code setPrintAbleReq(req)} / {@code setReq(req)}：设置请求体</li>
 *   <li>{@code collectResponseHeaders}：采集全部响应头</li>
 * </ul>
 * <p>常量：</p>
 * <ul>
 *   <li>{@code REQUEST_LOG_STR = "request-log"}：请求日志 logger 名称</li>
 *   <li>{@code EMPTY_REQ = "[no request body]"}：无请求体占位</li>
 *   <li>{@code EMPTY_RSP = "[no response body]"}：无响应体占位</li>
 *   <li>{@code STATIC_RESOURCE_EXCLUDE_PATTERNS}：内置需排除的静态资源默认路径模式（接口文档 webjars/swagger/doc.html、favicon、常见静态文件扩展名等）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code requestLogConfig} 未注入</td>
 *     <td>{@code isEnable}/{@code isExcludeUri} 经 {@code CObjUtils.convert} 判空，按默认值（false）处理</td>
 *   </tr>
 *   <tr>
 *     <td>{@code excludeUriPatterns} 为空</td>
 *     <td>用户排除为空，但仍排除内置静态资源默认路径（{@code isExcludeUri} 先判内置静态路径再读配置）</td>
 *   </tr>
 *   <tr>
 *     <td>uri 为内置静态资源</td>
 *     <td>{@code isExcludeUri} 返回 true（先于配置读取短路，无需容器/配置即可生效）</td>
 *   </tr>
 *   <tr>
 *     <td>uri 命中排除规则</td>
 *     <td>{@code genRequestLog} 返回 null，{@code init} 不绑定</td>
 *   </tr>
 *   <tr>
 *     <td>无当前请求日志</td>
 *     <td>{@code setReq}/{@code setRsp} 直接跳过（debug 日志提示）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>服务端 MVC、feign、resttemplate、httpclient 等请求方式构造并打印统一格式请求日志。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>ThreadLocal 需在请求结束时 {@code remove}，避免线程复用导致日志串线程；由拦截器 afterCompletion 统一处理。</li>
 *   <li>{@code EMPTY_REQ}/{@code EMPTY_RSP} 占位字符串所有请求共享，req 统一为 Object 直接存字符串与 feign 场景语义一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>ThreadLocal 传递</b></p>
 * <ul>
 *   <li>用 {@code ThreadLocal&lt;CRequestLog&gt;} 在请求处理链中传递请求日志，避免参数逐层传递。</li>
 * </ul>
 * <p><b>采集与打印解耦</b></p>
 * <ul>
 *   <li>采集层总是采集请求头存储；是否输出由打印层 {@code enableHeader} 开关控制。</li>
 * </ul>
 * <p><b>统一打印出口</b></p>
 * <ul>
 *   <li>{@code logWrite} 统一输出 HTTP 报文格式 dump，后续新增请求方式（resttemplate/httpclient 等）</li>
 *   <li>无需各自实现拼接逻辑。</li>
 * </ul>
 * <p><b>独立 Map 拷贝</b></p>
 * <ul>
 *   <li>{@code collectHeaders}/{@code collectResponseHeaders} 将 Servlet 请求/响应头视图遍历拷贝为独立 Map，</li>
 *   <li>避免日志模型持有请求对象内部结构。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/6
 * @version 1.0
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
     * <ul>
     *   <li>{@code isEnable()}：判断请求日志功能是否开启（{@code requestLogConfig.enable}，未注入时按 false）</li>
     * </ul>
     *
     * @return true 表示开启
     */
    public boolean isEnable() {
        val enable = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getEnable);
        return BooleanUtil.isTrue(enable);
    }

    /**
     * 内置需排除的静态资源默认路径模式（无需配置即生效，避免日志刷屏）。
     * 与配置 {@code exclude-uri-patterns}（追加的用户规则）叠加，命中任一即排除
     */
    private final String[] STATIC_RESOURCE_EXCLUDE_PATTERNS = {
        // 接口文档（knife4j/swagger）静态资源
        "/webjars/**",
        "/webjars",
        "/swagger-resources",
        "/swagger-resources/**",
        "/v2/api-docs",
        "/v2/api-docs/**",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/doc.html",
        // 浏览器默认请求
        "/favicon.ico",
        // 常见静态文件扩展名
        "/**/*.js",
        "/**/*.css",
        "/**/*.png",
        "/**/*.jpg",
        "/**/*.jpeg",
        "/**/*.gif",
        "/**/*.svg",
        "/**/*.ico",
        "/**/*.woff",
        "/**/*.woff2",
        "/**/*.ttf",
        "/**/*.eot"
    };

    /**
     * 判断 uri 是否命中排除规则（用户配置 {@code excludeUriPatterns} 或内置静态资源默认路径，命中任一即排除）
     * <ul>
     *   <li>{@code isExcludeUri(uri)}：判断 uri 是否命中排除规则（支持 {@code *}/{@code **} 通配，经 {@code CPatternUtils.getUrlCache} 预编译匹配）。</li>
     * </ul>
     *
     * @param uri 请求 uri
     * @return true 表示命中排除规则
     */
    public boolean isExcludeUri(String uri) {

        if (isStaticResourceUri(uri)) {
            return true;
        }

        val excludeUriPatterns = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getExcludeUriPatterns);
        if (CollUtil.isEmpty(excludeUriPatterns)) {
            return false;
        }
        return excludeUriPatterns.stream()
            .anyMatch(pattern -> matchUri(uri, pattern));
    }

    /**
     * 判断 uri 是否命中内置静态资源默认排除路径
     *
     * @param uri 请求 uri
     * @return true 表示命中内置静态资源排除规则
     */
    private boolean isStaticResourceUri(String uri) {
        for (val pattern : STATIC_RESOURCE_EXCLUDE_PATTERNS) {
            if (matchUri(uri, pattern)) {
                return true;
            }
        }
        return false;
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
     * <ul>
     *   <li>{@code genRequestLog()}：根据当前请求生成请求日志；uri 命中排除规则返回 null</li>
     * </ul>
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
     *
     * <ul>
     *   <li>{@code init()}：生成并绑定请求日志到当前线程</li>
     * </ul>
     */
    public void init() {

        val requestLog = genRequestLog();
        if (null != requestLog) {
            REQUEST_LOG_THREAD_LOCAL.set(requestLog);
        }

    }

    /**
     * 移除当前线程绑定的请求日志
     *
     * <ul>
     *   <li>{@code remove()}：移除当前线程请求日志</li>
     * </ul>
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
     * <ul>
     *   <li>{@code setRsp(rsp, throwable, response)}：记录响应体、异常信息、响应状态码与响应头</li>
     * </ul>
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
     * <ul>
     *   <li>{@code logWrite(info, enableHeader)}：记录请求日志、设置属性、打印日志的统一出口，输出 HTTP 报文格式 dump</li>
     * </ul>
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
