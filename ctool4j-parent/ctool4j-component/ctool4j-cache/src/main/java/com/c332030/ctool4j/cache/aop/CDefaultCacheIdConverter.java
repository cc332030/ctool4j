package com.c332030.ctool4j.cache.aop;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * Description: CDefaultCacheIdConverter
 * </p>
 *
 * @since 2025/9/27
 */
public class CDefaultCacheIdConverter implements ICCacheIdConverter<Object, Object> {

    @Override
    public String applyThrowable(Object key, Object object) throws Throwable {

        if(null == key
            && null == object
        ) {
            return null;
        }

        if(null == key) {
            return StrUtil.toStringOrNull(object);
        }

        return StrUtil.toStringOrNull(key);
    }

}
