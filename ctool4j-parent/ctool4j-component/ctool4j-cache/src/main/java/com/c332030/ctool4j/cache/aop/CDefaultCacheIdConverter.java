package com.c332030.ctool4j.cache.aop;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * Description: CDefaultCacheIdConverter
 * </p>
 *
 * @see "doc/design/cache/CDefaultCacheIdConverter.adoc"
 * @see "doc/design/cache/CDefaultCacheIdConverterTests.adoc"
 * @since 2025/9/27
 */
public class CDefaultCacheIdConverter implements ICCacheIdConverter<Object, Object> {

    /**
     * 生成缓存 id：优先取 key，key 为空时取 object 的字符串形式，都为空返回 null
     *
     * @param key    缓存 key
     * @param object 缓存对象
     * @return 缓存 id
     */
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
