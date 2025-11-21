package com.c332030.ctool4j.spring.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.google.common.net.HttpHeaders;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
     */
    public boolean hasRequest() {
        return null != getServletRequestAttributes();
    }

    /**
     * @see CRequestUtils#hasRequest()
     */
    public boolean noRequest() {
        return !hasRequest();
    }

    public HttpServletRequest getRequestDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getRequest();
    }

    public HttpServletRequest getRequest() {
        return Optional.ofNullable(getRequestDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("request 不能为空"));
    }

    public HttpServletResponse getResponseDefaultNull() {
        val springServletRequestAttributes = getServletRequestAttributes();
        if(null == springServletRequestAttributes) {
            return null;
        }
        return springServletRequestAttributes.getResponse();
    }

    public HttpServletResponse getResponse() {
        return Optional.ofNullable(getResponseDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("response 不能为空"));
    }

    public String getContextPathDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getContextPath);
    }

    public String getContextPath() {
        return Optional.ofNullable(getContextPathDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("contextPath 不能为空"));
    }

    public String getRequestURIDefaultNull() {
        return CObjUtils.convert(getRequestDefaultNull(), HttpServletRequest::getRequestURI);
    }

    public String getRequestURI() {
        return Optional.ofNullable(getRequestURIDefaultNull())
                .orElseThrow(() -> new IllegalArgumentException("requestURI 不能为空"));
    }

    public String getHeader(String header) {
        return getHeader(getRequest(), header);
    }

    public String getHeader(HttpServletRequest request, String header) {
        return request.getHeader(header);
    }

    public void getHeaderThenDo(Collection<String> headerNames, BiConsumer<String, String> biConsumer) {

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

    public String getIp() {
        return getIp(getRequest());
    }

    public String getIp(HttpServletRequest request) {

        String forwardIpBundle = getHeader(request, HttpHeaders.X_FORWARDED_FOR);
        if (StringUtils.isNotEmpty(forwardIpBundle)) {
            String[] forwardIpParts = forwardIpBundle.split(",");
            return forwardIpParts[0];
        }

        return request.getRemoteAddr();
    }

}
