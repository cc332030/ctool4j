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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

/**
 * <p>
 * Description: RequestUtils
 * </p>
 *
 * @since 2024/12/9
 */
@CustomLog
@UtilityClass
public class CRequestUtils {

    /**
     * 请求前-初始化
     */
    private static final Set<BiConsumer<HttpServletRequest, HttpServletResponse>> PREPARE_CONSUMERS = new CopyOnWriteArraySet<>();

    public void addPrepare(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        PREPARE_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

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

    public void addClear(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        CLEAR_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    public void clear(HttpServletRequest request, HttpServletResponse response) {
        CLEAR_CONSUMERS.forEach(consumer -> {
            try {
                consumer.accept(request, response);
            } catch (Throwable t) {
                log.error("clear failure", t);
            }
        });
    }

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
     * @return
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

}
