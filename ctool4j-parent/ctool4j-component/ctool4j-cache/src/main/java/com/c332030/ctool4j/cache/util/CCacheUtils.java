package com.c332030.ctool4j.cache.util;

import com.c332030.ctool4j.cache.service.CCacheService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CCacheUtils
 * </p>
 *
 * @since 2025/9/27
 */
@UtilityClass
@CAutowiredScan
public class CCacheUtils {

    @Setter
    @CAutowired
    CCacheService cacheService;

    public <T> CCacheService.CCacheBuilder<T> cacheBuilder(String key, Class<T> tClass) {
        return cacheService.cacheBuilder(key, tClass);
    }

}
