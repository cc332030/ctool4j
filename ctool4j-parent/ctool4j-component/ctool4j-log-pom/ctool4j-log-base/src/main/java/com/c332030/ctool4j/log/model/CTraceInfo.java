package com.c332030.ctool4j.log.model;

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
