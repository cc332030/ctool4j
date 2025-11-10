package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.core.interfaces.IOperate;
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

    public static String getLockKey(Class<?> clazz, IOperate iOperate, Object key) {
        return getLockKey(CRedisUtils.getKey(clazz, iOperate, key));
    }

}
