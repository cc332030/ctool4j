package com.c332030.ctool4j.csv.util;

import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CCsvUtils
 * </p>
 *
 * @since 2026/1/14
 */
@UtilityClass
public class CCsvUtils {

    public static <T> CCsvBuilder<T> builder(Class<T> headClass) {
        return new CCsvBuilder<>(headClass);
    }

    public String trim(String str) {
        return CStrUtils.trim(str)
            .replaceAll("\b", "")
            ;
    }

}
