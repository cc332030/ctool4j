package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.definition.function.CBiFunction;

/**
 * <p>
 * Description: ICCacheIdConverter
 * </p>
 *
 * @since 2025/9/27
 */
public interface ICCacheIdConverter<KEY, CLASS> extends CBiFunction<KEY, CLASS, String> {

    @Override
    String applyThrowable(KEY key, CLASS object) throws Throwable;

}
