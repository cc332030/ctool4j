package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConverter;
import com.c332030.ctool4j.cache.aop.ICCacheIdConverter;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheable
 * </p>
 *
 * @since 2025/9/27
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheable {

    /**
     * 是否本地缓存
     * @return true 本地缓存，false redis 缓存
     */
    boolean local() default true;

    /**
     * 缓存 id 生成类
     * @return 缓存 id 生成类
     */
    Class<? extends ICCacheIdConverter<?, ?>> idConverter() default CDefaultCacheIdConverter.class;

    /**
     * 缓存命名空间类
     * @return 缓存命名空间类
     */
    Class<?> namespace();

    /**
     * 缓存过期时间，单位秒，为 0 则为永久
     * @return 缓存过期时间
     */
    long expire() default 0;

}
