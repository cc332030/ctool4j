package com.c332030.ctool4j.feign.annotation;

import com.c332030.ctool4j.feign.interceptor.ICRequestInterceptor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCustomerFeignInterceptor
 * </p>
 *
 * @since 2025/12/26
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCustomerFeignInterceptor {

    Class<? extends ICRequestInterceptor> value();

}
