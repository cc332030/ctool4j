package com.c332030.ctool4j.spring.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.COpt;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.google.common.net.HttpHeaders;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CRequestUtils
 * </p>
 *
 * @since 2024/12/9
 * @see "doc/design/spring/CRequestUtils.adoc"
 * @see "doc/design/spring/CRequestUtilsTests.adoc"
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
     * 获取当前请求的属性
     *
     * @return 当前请求的属性
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
     * 获取 Request，可为空
     * @return HttpServletRequest
     */
    public HttpServletRequest getRequestDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getRequest();
    }

    /**
     * 获取 Request 的 COpt
     * @return COpt HttpServletRequest
     */
    public COpt<HttpServletRequest> getRequestOpt() {
        return COpt.ofNullable(getRequestDefaultNull());
    }

    /**
     * 获取 Request，不能为空
     * @return Request，不能为空
     */
    public HttpServletRequest getRequest() {
        return getRequestOpt()
                .orElseThrow(() -> new IllegalArgumentException("request 不能为空"));
    }

    /**
     * 获取 Response，可为空
     * @return HttpServletResponse
     */
    public HttpServletResponse getResponseDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getResponse();
    }

    /**
     * 获取 Response 的 COpt
     * @return COpt HttpServletResponse
     */
    public COpt<HttpServletResponse> getResponseOpt() {
        return COpt.ofNullable(getResponseDefaultNull());
    }

    /**
     * 获取 Response，不能为空
     * @return HttpServletResponse
     */
    public HttpServletResponse getResponse() {
        return getResponseOpt()
                .orElseThrow(() -> new IllegalArgumentException("response 不能为空"));
    }

    /**
     * 获取 Context Path，可为空
     * @return Context Path
     */
    public String getContextPathDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getContextPath);
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
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getRequestURI);
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
     * @param request HttpServletRequest
     * @param header Header Name
     * @return Header Value
     */
    public String getHeader(HttpServletRequest request, String header) {
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
     * @param request HttpServletRequest
     * @param header Header Name
     * @return Header Values
     */
    public List<String> getHeaders(HttpServletRequest request, String header) {
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
     * @param request HttpServletRequest
     * @return Ip
     */
    public String getIp(HttpServletRequest request) {

        String forwardIpBundle = getHeader(request, HttpHeaders.X_FORWARDED_FOR);
        if (StrUtil.isNotEmpty(forwardIpBundle)) {
            String[] forwardIpParts = forwardIpBundle.split(",");
            return forwardIpParts[0];
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取请求属性并转为字符串（null 属性返回 null），可为空
     * @param request       HttpServletRequest
     * @param attributeName 属性名
     * @return 属性字符串
     */
    public String getAttrStr(HttpServletRequest request, String attributeName) {
        return StrUtil.toStringOrNull(request.getAttribute(attributeName));
    }

    /**
     * 获取错误状态码（取自 RequestDispatcher.ERROR_STATUS_CODE 属性），可为空
     * @param request HttpServletRequest
     * @return 错误状态码字符串
     */
    public String getErrorStatusCode(HttpServletRequest request) {
        return getAttrStr(request, RequestDispatcher.ERROR_STATUS_CODE);
    }

}
