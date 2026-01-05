package com.c332030.ctool4j.feign.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.feign.config.CFeignClientHeaderConfig;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import feign.RequestTemplate;
import feign.Response;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CFeignUtils
 * </p>
 *
 * @since 2025/9/21
 */
@CustomLog
@UtilityClass
public class CFeignUtils {

    public final ThreadLocal<StringBuilder> HTTP_LOG_THREAD_LOCAL = ThreadLocal.withInitial(StringBuilder::new);

    private static final Map<Class<?>, CConsumer<RequestTemplate>> INTERCEPTOR_MAP = new ConcurrentHashMap<>();

    @CAutowired
    CFeignClientHeaderConfig headerConfig;

    public void addInterceptor(Class<?> clazz, CConsumer<RequestTemplate> consumer) {

        log.debug("addInterceptor to: {}, consumer: {}", clazz, consumer);
        INTERCEPTOR_MAP.put(clazz, consumer);

    }

    public Class<?> getApiType(RequestTemplate template) {
        return template.feignTarget().type();
    }

    public boolean intercept(RequestTemplate template) {
        val type = getApiType(template);
        return intercept(type, template);
    }

    public boolean intercept(Class<?> type, RequestTemplate template) {

        for (val entry : INTERCEPTOR_MAP.entrySet()) {
            if(entry.getKey().isAssignableFrom(type)) {
                entry.getValue().accept(template);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建新的响应
     * @param response 原始响应
     * @param responseBodyBytes 响应体字节数组
     * @return 新的响应
     */
    public Response newResponse(Response response, byte[] responseBodyBytes) {

        val request = response.request();

        return Response.builder()
            .requestTemplate(request.requestTemplate())
            // TODO 低版本不支持
            // .protocolVersion(response.protocolVersion())
            .status(response.status())
            .reason(response.reason())
            .request(request)
            .headers(response.headers())
            .body(responseBodyBytes)
            .build();
    }

    public void transferHeaders(RequestTemplate template) {

        if(null == headerConfig) {
            return;
        }

        val propagationMode = headerConfig.getPropagationMode();
        val propagationRequestHeaders = headerConfig.getPropagationRequestHeaders();

        if(propagationMode == CFeignClientHeaderPropagationModeEnum.ALL
            && CollUtil.isEmpty(propagationRequestHeaders)
        ) {
            log.debug("propagation all headers and no propagation request headers");
            return;
        }

        val propagationCustomHeaders = headerConfig.getPropagationCustomHeaders();

        val originHeaders = CMapUtils.defaultEmpty(template.headers());
        val newHeaders = new LinkedHashMap<String, Collection<String>>(
            propagationCustomHeaders.size() + propagationRequestHeaders.size()
        );

        switch (propagationMode) {
            case ALL:
                newHeaders.putAll(originHeaders);
                break;
            case CUSTOM:
                originHeaders.forEach((header, values) -> {
                    if(propagationCustomHeaders.contains(header)
                        && CollUtil.isNotEmpty(values)
                    ) {
                        newHeaders.put(header, values);
                    }
                });
                break;
            case NONE:
                break;
        }
        log.debug("propagationMode: {}, propagationCustomHeaders: {}, propagationRequestHeaders",
            propagationMode, propagationCustomHeaders, propagationRequestHeaders);

        CRequestUtils.getHeadersThenDo(propagationRequestHeaders, newHeaders::put);

        log.debug("newHeaders: {}", newHeaders);
        template.headers(null);
        template.headers(newHeaders);

    }


}
