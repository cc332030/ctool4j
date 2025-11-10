package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.core.interfaces.IOperate;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CRedisUtils
 * </p>
 *
 * @since 2025/11/10
 */
@UtilityClass
public class CRedisUtils {

    public static final String KEY_SEPARATOR = ":";

    public static String getKey(Class<?> clazz, IOperate iOperate, Object key) {
        return CSpringUtils.getApplicationName()
                + KEY_SEPARATOR
                + clazz.getSimpleName()
                + KEY_SEPARATOR
                + iOperate.getName()
                + KEY_SEPARATOR
                + key
                ;
    }

}
