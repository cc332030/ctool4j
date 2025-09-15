package com.c332030.ctool.redis.util;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: LockUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/20
 */
@UtilityClass
public class CLockUtils {

    public static String getLockKey(String key) {
        return key + "-lock";
    }

    public static String getLockKey(Class<?> clazz, String operate, String key) {
        return key + "-lock";
    }

}
