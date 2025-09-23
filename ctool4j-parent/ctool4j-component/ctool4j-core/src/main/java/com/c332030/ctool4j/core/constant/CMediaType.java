package com.c332030.ctool4j.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

/**
 * <p>
 *   Description: CMediaType
 * </p>
 *
 * @author c332030
 * @since 2024/10/23
 */
@UtilityClass
public class CMediaType {

    public static final String APPLICATION_JSON5_VALUE = "application/json5";

    public static final MediaType APPLICATION_JSON5 = MediaType.parseMediaType(APPLICATION_JSON5_VALUE);

}
