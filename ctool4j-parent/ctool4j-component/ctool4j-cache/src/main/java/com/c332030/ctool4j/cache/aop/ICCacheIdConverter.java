package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.definition.function.CBiFunction;

/**
 * <p>
 * Description: ICCacheIdConverter
 * </p>
 *
 * @see "doc/design/cache/ICCacheIdConverter.adoc"
 * @see "doc/design/cache/CDefaultCacheIdConverterTests.adoc"
 * @since 2025/9/27
 */
public interface ICCacheIdConverter<KEY, CLASS> extends CBiFunction<KEY, CLASS, String> {

    /**
     * 生成缓存ID，可抛出受检异常
     * @param key 缓存键
     * @param object 缓存对象
     * @return 缓存ID
     * @throws Throwable 生成过程中可能抛出的异常
     */
    @Override
    String applyThrowable(KEY key, CLASS object) throws Throwable;

}
