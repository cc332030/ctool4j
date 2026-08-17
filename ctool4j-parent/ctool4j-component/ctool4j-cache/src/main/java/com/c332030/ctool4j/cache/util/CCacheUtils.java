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

    /**
     * 获取缓存构建器
     *
     * @param key    缓存 key
     * @param tClass 缓存值类型
     * @param <T>    缓存值类型
     * @return 缓存构建器
     */
    public <T> CCacheService.CCacheBuilder<T> cacheBuilder(String key, Class<T> tClass) {
        return cacheService.cacheBuilder(key, tClass);
    }

}
