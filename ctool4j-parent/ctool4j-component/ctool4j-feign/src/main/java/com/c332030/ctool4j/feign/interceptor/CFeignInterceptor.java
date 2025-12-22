package com.c332030.ctool4j.feign.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.feign.config.CFeignClientHeaderConfig;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * <p>
 * Description: CFeignInterceptor
 * </p>
 *
 * @since 2025/9/21
 */
@CustomLog
@AllArgsConstructor
public class CFeignInterceptor implements RequestInterceptor {

    CFeignClientHeaderConfig headerConfig;

    @Override
    public void apply(RequestTemplate template) {
        try {
            dealHeaders(template);
            CFeignUtils.intercept(template);
        } catch (Throwable t) {
            log.error("dealHeaders error", t);
        }
    }

    private void dealHeaders(RequestTemplate template) {

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
