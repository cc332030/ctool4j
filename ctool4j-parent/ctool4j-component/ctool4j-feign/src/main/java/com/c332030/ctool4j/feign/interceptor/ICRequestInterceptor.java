package com.c332030.ctool4j.feign.interceptor;

import com.c332030.ctool4j.definition.function.CConsumer;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * <p>
 * Description: ICRequestInterceptor
 * </p>
 *
 * @see doc/design/feign/ICRequestInterceptor.adoc
 * @since 2025/12/26
 */
@FunctionalInterface
public interface ICRequestInterceptor extends RequestInterceptor, CConsumer<RequestTemplate> {

    /**
     * 处理请求模板（委托给 accept）
     * @param requestTemplate 请求模板
     */
    @Override
    default void apply(RequestTemplate requestTemplate) {
        accept(requestTemplate);
    }

}
