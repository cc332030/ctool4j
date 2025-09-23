package com.c332030.ctool4j.feign.util;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CFeignUtils
 * </p>
 *
 * @since 2025/9/21
 */
@UtilityClass
public class CFeignUtils {

    public static final ThreadLocal<StringBuilder> HTTP_LOG_THREAD_LOCAL = ThreadLocal.withInitial(StringBuilder::new);

}
