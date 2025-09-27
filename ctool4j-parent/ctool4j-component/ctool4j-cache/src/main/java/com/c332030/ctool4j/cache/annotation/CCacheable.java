package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConvert;
import com.c332030.ctool4j.cache.aop.ICCacheIdConvert;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheable
 * </p>
 *
 * @since 2025/9/27
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CCacheable {

    boolean local() default true;

    Class<? extends ICCacheIdConvert<?>> idConverter() default CDefaultCacheIdConvert.class;

}
