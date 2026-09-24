package com.c332030.ctool4j.spring.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.COpt;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.model.CHttpServletResponse;
import com.google.common.net.HttpHeaders;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CRequestUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRequestUtils}：请求工具，读取当前请求上下文（Spring 的 {@code RequestContextHolder}）中的
 * 请求/响应，并提供请求 URI、头、IP、属性、错误状态码等取用。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>对外只暴露抽象层类型</b>：请求/响应的取用入口（{@code getRequest} / {@code getResponse} 系列）
 *   返回 {@link CHttpRequest} / {@link CHttpResponse}，业务模块据此编程即不绑定任何 Servlet 包；
 *   本类在两侧适配模块中<b>同包同名</b>（本份承接 jakarta 侧），切换容器只换依赖模块。</li>
 *   <li><b>容器逃生口只有一处</b>：{@link #getServletRequestAttributes()} 返回 Spring 的
 *   {@code ServletRequestAttributes}，其请求/响应是某一容器包的类型，只供抽象层表达不了的容器专有能力
 *   （二进制输出流、会话、Cookie）使用；其余场景一律走抽象层入口。</li>
 *   <li><b>IP 单一来源</b>：IP 解析规则收在 {@link CHttpRequest#getClientIp()}，本类不另立一套（见 {@link #getIp}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>缺失返回默认：非请求上下文时 {@code getXxxDefaultNull} 返回 {@code null}、{@code getXxx} 抛
 * {@link IllegalArgumentException}；{@code getRefererPathThenConvertDefaultNull} 异常时返回 {@code null}。</p>
 * <h2>适用范围</h2>
 * <p>请求处理线程内取用当前请求/响应；需要在两套 Servlet 容器间可切换的公共代码。</p>
 * <h2>不适用与边界场景</h2>
 * <p>非请求线程（定时任务、异步线程）取不到上下文；需要容器专有能力时经
 * {@link #getServletRequestAttributes()} 取底层对象（容器类型只在调用处出现一次）。</p>
 * <h2>已知限制与取舍</h2>
 * <p>静态工具类，依赖 Spring 的请求上下文线程绑定；{@link #getServletRequestAttributes()} 之外的入口
 * 都经过了抽象层包装（每次取用新建一个适配器实例，成本为一次对象创建）。</p>
 *
 * @since 2024/12/9
 * @version 1.1
 */
@CustomLog
@UtilityClass
public class CRequestUtils {

    /**
     * 请求前-初始化
     */
    private static final Set<BiConsumer<HttpServletRequest, HttpServletResponse>> PREPARE_CONSUMERS = new CopyOnWriteArraySet<>();

    /**
     * 注册请求前初始化回调
     *
     * @param consumer 请求前初始化回调
     */
    public void addPrepare(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        PREPARE_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    /**
     * 执行全部请求前初始化回调
     *
     * @param request  请求
     * @param response 响应
     */
    public void prepare(HttpServletRequest request, HttpServletResponse response) {
        PREPARE_CONSUMERS.forEach(consumer -> {
            try {
                consumer.accept(request, response);
            } catch (Throwable t) {
                log.error("clear failure", t);
            }
        });
    }

    /**
     * 请求后-清洁工作
     */
    private static final Set<BiConsumer<HttpServletRequest, HttpServletResponse>> CLEAR_CONSUMERS =
            new CopyOnWriteArraySet<>();

    /**
     * 注册请求结束清理回调
     *
     * @param consumer 请求结束清理回调
     */
    public void addClear(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        CLEAR_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    /**
     * 执行全部请求结束清理回调
     *
     * @param request  请求
     * @param response 响应
     */
    public void clear(HttpServletRequest request, HttpServletResponse response) {
        CLEAR_CONSUMERS.forEach(consumer -> {
            try {
                consumer.accept(request, response);
            } catch (Throwable t) {
                log.error("clear failure", t);
            }
        });
    }

    /**
     * 获取当前请求的属性（Spring 的请求上下文）
     *
     * <p>返回 Spring 自己的 {@code ServletRequestAttributes}，其 {@code getRequest()}/{@code getResponse()}
     * 都是某一容器包（javax 或 jakarta）的类型。本方法是本类的<b>唯一容器逃生口</b>，只供抽象层表达不了的
     * 容器专有能力（二进制输出流、会话、Cookie）使用；其余场景一律走 {@link #getRequest()} /
     * {@link #getResponse()} 这类只暴露抽象层类型的入口。</p>
     *
     * @return 当前请求的属性；非请求上下文时返回 null
     */
    public static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 是否是接口请求
     * @return 结果
     */
    public boolean hasRequest() {
        return null != getServletRequestAttributes();
    }

    /**
     * @see CRequestUtils#hasRequest()
     * @return 结果
     */
    public boolean noRequest() {
        return !hasRequest();
    }

    /**
     * 获取请求（抽象层），可为空
     *
     * @return 抽象层请求；非请求上下文时返回 null
     */
    public CHttpRequest getRequestDefaultNull() {
        val request = CObjUtils.convert(getServletRequestAttributes(), ServletRequestAttributes::getRequest);
        if(null == request) {
            return null;
        }
        return CHttpServletRequest.of(request);
    }

    /**
     * 获取请求（抽象层）的 COpt
     *
     * @return COpt 抽象层请求
     */
    public COpt<CHttpRequest> getRequestOpt() {
        return COpt.ofNullable(getRequestDefaultNull());
    }

    /**
     * 获取请求（抽象层），不能为空
     *
     * @return 抽象层请求，不能为空
     */
    public CHttpRequest getRequest() {
        return getRequestOpt()
                .orElseThrow(() -> new IllegalArgumentException("request 不能为空"));
    }

    /**
     * 获取响应（抽象层），可为空
     *
     * @return 抽象层响应；非请求上下文或容器未创建响应时返回 null
     */
    public CHttpResponse getResponseDefaultNull() {
        val response = CObjUtils.convert(getServletRequestAttributes(), ServletRequestAttributes::getResponse);
        if(null == response) {
            return null;
        }
        return CHttpServletResponse.of(response);
    }

    /**
     * 获取响应（抽象层）的 COpt
     *
     * @return COpt 抽象层响应
     */
    public COpt<CHttpResponse> getResponseOpt() {
        return COpt.ofNullable(getResponseDefaultNull());
    }

    /**
     * 获取响应（抽象层），不能为空
     *
     * @return 抽象层响应，不能为空
     */
    public CHttpResponse getResponse() {
        return getResponseOpt()
                .orElseThrow(() -> new IllegalArgumentException("response 不能为空"));
    }

    /**
     * 获取 Context Path，可为空
     * @return Context Path
     */
    public String getContextPathDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), CHttpRequest::getContextPath);
    }

    /**
     * 获取 Context Path，不能为空
     * @return Context Path
     */
    public String getContextPath() {
        return Optional.ofNullable(getContextPathDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("contextPath 不能为空"));
    }

    /**
     * 获取 RequestURI，可为空
     * @return RequestURI
     */
    public String getRequestURIDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), CHttpRequest::getRequestURI);
    }

    /**
     * 获取 RequestURI，不能为空
     * @return RequestURI
     */
    public String getRequestURI() {
        return Optional.ofNullable(getRequestURIDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("requestURI 不能为空"));
    }

    /**
     * 获取 Header
     * @param header Header Name
     * @return Header Value
     */
    public String getHeader(String header) {
        return getHeader(getRequest(), header);
    }

    /**
     * 获取 Header
     * @param request 抽象层请求
     * @param header Header Name
     * @return Header Value
     */
    public String getHeader(CHttpRequest request, String header) {
        return request.getHeader(header);
    }

    /**
     * 获取 Headers
     * @param header Header Name
     * @return Header Values
     */
    public List<String> getHeaders(String header) {
        return getHeaders(getRequest(), header);
    }

    /**
     * 获取 Headers
     * @param request 抽象层请求
     * @param header Header Name
     * @return Header Values
     */
    public List<String> getHeaders(CHttpRequest request, String header) {
        val valueEnumeration = request.getHeaders(header);
        return CCollUtils.getValues(valueEnumeration);
    }

    /**
     * 获取 Header，并做动作
     * @param headerNames Header Names
     * @param biConsumer 动作
     */
    public void getHeaderThenDo(Collection<String> headerNames, CBiConsumer<String, String> biConsumer) {

        if(CollUtil.isEmpty(headerNames)){
            return;
        }

        val request = getRequestDefaultNull();
        if(null == request) {
            return;
        }
        headerNames.forEach(headerName -> {

            val headerValue = getHeader(request, headerName);
            if (StrUtil.isEmpty(headerValue)) {
                return;
            }
            biConsumer.accept(headerName, headerValue);
        });

    }

    /**
     * 获取 Header，并做动作
     * @param headerNames Header Names
     * @param biConsumer 动作
     */
    public void getHeadersThenDo(Collection<String> headerNames, CBiConsumer<String, List<String>> biConsumer) {

        if(CollUtil.isEmpty(headerNames)){
            return;
        }
        val request = getRequestDefaultNull();
        if(null == request) {
            return;
        }
        headerNames.forEach(headerName -> {

            val headerValues = getHeaders(request, headerName);
            if (CollUtil.isEmpty(headerValues)) {
                return;
            }
            biConsumer.accept(headerName, headerValues);
        });

    }

    /**
     * 获取 Referer
     * @return Referer
     */
    public String getReferer() {
        return getRequest().getHeader(HttpHeaders.REFERER);
    }

    /**
     * 获取 Referer Path
     * @return Referer Path
     */
    public String getRefererPath() {
        return CUrlUtils.getPath(getReferer());
    }

    /**
     * 获取 Referer Path，并做转换
     * @param function 转换方法
     * @return 目标
     * @param <T> 目标泛型
     */
    public <T> T getRefererPathThenConvert(StringFunction<T> function) {
        val path = getRefererPath();
        if(StrUtil.isEmpty(path)) {
            return null;
        }
        return function.apply(path);
    }

    /**
     * 获取 Referer Path，并做转换，默认为空
     * @param function 转换方法
     * @return 目标
     * @param <T> 目标泛型
     */
    public <T> T getRefererPathThenConvertDefaultNull(StringFunction<T> function) {
        try {
            return getRefererPathThenConvert(function);
        } catch (Exception e) {
            log.debug("转换 Referer 失败", e);
            return null;
        }
    }

    /**
     * 获取 Ip
     * @return Ip
     */
    public String getIp() {
        return getIp(getRequest());
    }

    /**
     * 获取 Ip
     * <p>注意：当前实现无条件信任 X-Forwarded-For 首段，客户端直连时
     * 可伪造该请求头绕过 IP 校验/风控，属安全问题。
     * 修复方案：仅信任来自已配置可信代理的 X-Forwarded-For（默认不信任，
     * 未配置时忽略该头直接返回 remoteAddr）；当前业务场景较小，暂未修复</p>
     * @param request 抽象层请求
     * @return Ip
     */
    public String getIp(CHttpRequest request) {
        // 单一来源：IP 解析规则收在抽象层的 CHttpRequest#getClientIp
        return request.getClientIp();
    }

    /**
     * 获取请求属性并转为字符串（null 属性返回 null），可为空
     * @param request       抽象层请求
     * @param attributeName 属性名
     * @return 属性字符串
     */
    public String getAttrStr(CHttpRequest request, String attributeName) {
        return StrUtil.toStringOrNull(request.getAttribute(attributeName));
    }

    /**
     * 获取错误状态码（取自 RequestDispatcher.ERROR_STATUS_CODE 属性），可为空
     * @param request 抽象层请求
     * @return 错误状态码字符串
     */
    public String getErrorStatusCode(CHttpRequest request) {
        return getAttrStr(request, RequestDispatcher.ERROR_STATUS_CODE);
    }

}
