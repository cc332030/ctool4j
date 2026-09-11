package com.c332030.ctool4j.feign.annotation;

import com.c332030.ctool4j.feign.interceptor.ICRequestInterceptor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCustomerFeignInterceptor
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅作用于类型（{@code @Target(TYPE)}）、运行时保留、可继承。</li>
 *   <li>通过 {@code value()} 声明拦截器实现类，供 {@code CFeignUtils.addInterceptor} 按接口类型注册。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无（注解本身无逻辑；未标注的接口使用全局拦截器）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要为特定 Feign 接口定制请求拦截（加签、透传特殊 header）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅声明拦截器类；实例装配由调用方/配置完成。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>每接口单一拦截器；多拦截器需自行组合。</li>
 * </ul>
 *
 * @since 2025/12/26
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCustomerFeignInterceptor {

    Class<? extends ICRequestInterceptor> value();

}
