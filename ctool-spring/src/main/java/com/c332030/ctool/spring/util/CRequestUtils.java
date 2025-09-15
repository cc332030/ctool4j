package com.c332030.ctool.spring.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool.core.function.StringFunction;
import com.c332030.ctool.core.util.CObjUtils;
import com.c332030.ctool.core.util.CUrlUtils;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public static void addPrepare(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        PREPARE_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    public static void prepare(HttpServletRequest request, HttpServletResponse response) {
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

    public static void addClear(BiConsumer<HttpServletRequest, HttpServletResponse> consumer) {
        CLEAR_CONSUMERS.add(Objects.requireNonNull(consumer));
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
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
     */
    public static boolean hasRequest() {
        return null != getServletRequestAttributes();
    }

    /**
     * @see CRequestUtils#hasRequest()
     */
    public static boolean noRequest() {
        return !hasRequest();
    }

    public static HttpServletRequest getRequestDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getRequest();
    }

    public static HttpServletRequest getRequest() {
        return Optional.ofNullable(getRequestDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("request 不能为空"));
    }

    public static HttpServletResponse getResponseDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getResponse();
    }

    public static HttpServletResponse getResponse() {
        return Optional.ofNullable(getResponseDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("response 不能为空"));
    }

    public static String getContextPathDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getContextPath);
    }

    public static String getContextPath() {
        return Optional.ofNullable(getContextPathDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("contextPath 不能为空"));
    }

    public static String getRequestURIDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getRequestURI);
    }

    public static String getRequestURI() {
        return Optional.ofNullable(getRequestURIDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("requestURI 不能为空"));
    }

    public static String getHeader(String header) {
        return getHeader(getRequest(), header);
    }

    public static String getHeader(HttpServletRequest request, String header) {
        return request.getHeader(header);
    }

    public static void getHeaderThenDo(Collection<String> headerNames, BiConsumer<String, String> biConsumer) {

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

    public String getReferer() {
        return getRequest().getHeader(HttpHeaders.REFERER);
    }

    public String getRefererPath() {
        return CUrlUtils.getPath(getReferer());
    }

    public <T> T getRefererPathThenConvert(StringFunction<T> function) {
        val path = getRefererPath();
        if(StrUtil.isEmpty(path)) {
            return null;
        }
        return function.apply(path);
    }

    public <T> T getRefererPathThenConvertDefaultNull(StringFunction<T> function) {
        try {
            return getRefererPathThenConvert(function);
        } catch (Exception e) {
            log.debug("转换 Referer 失败", e);
            return null;
        }
    }

}
