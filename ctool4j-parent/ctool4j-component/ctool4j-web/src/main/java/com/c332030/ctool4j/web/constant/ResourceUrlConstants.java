package com.c332030.ctool4j.web.constant;

import com.c332030.ctool4j.core.util.CSet;
import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * <p>
 * Description: ResourceUrlConstants
 * </p>
 *
 * @since 2026/1/28
 */
@UtilityClass
public class ResourceUrlConstants {

    public final String FAVICON_ICO_URL = "/favicon.ico";

    public final Set<String> IGNORE_RESOURCE_URLS = CSet.of(
        FAVICON_ICO_URL
    );

}
