package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Set;

/**
 * <p>
 * Description: CMediaTypeUtils
 * </p>
 *
 * @since 2025/9/21
 */
@UtilityClass
public class CMediaTypeUtils {

    public static final Set<String> TEXT_KEYS = CSet.of(
            "text",
            "plain",
            "html",
            "json",
            "xml",
            "form"
    );

    public boolean isText(String mediaType) {
        for (val key : TEXT_KEYS) {
            if (mediaType.contains(key)) {
                return true;
            }
        }
        return false;
    }

}
