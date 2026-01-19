package com.c332030.ctool4j.excel.util;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CExcelUtils
 * </p>
 *
 * @since 2026/1/14
 */
@UtilityClass
public class CExcelUtils {

    public static <T> CExcelBuilder<T> builder() {
        return new CExcelBuilder<>();
    }

    public static <T> CExcelBuilder<T> builder(Class<T> headClass) {
        return new CExcelBuilder<>(headClass);
    }

}
