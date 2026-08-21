package com.c332030.ctool4j.web.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CTraceInfo
 * </p>
 *
 * @since 2025/9/26
 * @see doc/design/web/CTraceInfo.adoc
 * @see doc/design/web/CTraceInfoTests.adoc
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CTraceInfo implements ICTraceInfo {

    /**
     * 跟踪id
     */
    String traceId;

}
