package com.c332030.ctool4j.feign.interceptor;

import com.c332030.ctool4j.definition.function.CConsumer;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * <p>
 * Description: ICRequestInterceptor
 * </p>
 *
 * @since 2025/12/26
 */
@FunctionalInterface
public interface ICRequestInterceptor extends RequestInterceptor, CConsumer<RequestTemplate> {

    @Override
    default void apply(RequestTemplate requestTemplate) {
        accept(requestTemplate);
    }

}
