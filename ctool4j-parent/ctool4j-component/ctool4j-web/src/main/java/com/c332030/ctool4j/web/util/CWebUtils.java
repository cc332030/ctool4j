package com.c332030.ctool4j.web.util;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CWebUtils
 * </p>
 *
 * @since 2025/9/28
 */
@UtilityClass
public class CWebUtils {

    public String getContentDispositionValue(String filename) {
        return "attachment;filename=" + filename;
    }

}
