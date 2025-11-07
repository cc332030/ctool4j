package com.c332030.ctool4j.cache.aop;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * Description: CDefaultCacheIdConvert
 * </p>
 *
 * @since 2025/9/27
 */
public class CDefaultCacheIdConvert implements ICCacheIdConvert<Object>{

    @Override
    public String applyThrowable(Object o) {
        return StrUtil.toStringOrNull(o);
    }

}
