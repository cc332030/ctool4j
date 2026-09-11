package com.c332030.ctool4j.feign.interceptor;

import com.c332030.ctool4j.definition.function.CConsumer;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * <p>
 * Description: ICRequestInterceptor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRequestInterceptor}（{@code @FunctionalInterface}）扩展 feign {@code RequestInterceptor} 与 {@code CConsumer&lt;RequestTemplate&gt;}， 将 {@code apply(requestTemplate)} 委托给 {@code accept(requestTemplate)}。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>{@code apply} 默认实现委托给 {@code CConsumer.accept}，支持函数式写法（lambda 直接实现 accept）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（接口默认委托，实现方实现 accept）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>自定义 Feign 请求拦截器（配合 {@code @CCustomerFeignInterceptor}）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅委托；不提供默认拦截逻辑。</li>
 * </ul>
 *
 * @since 2025/12/26
 * @version 1.0
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
