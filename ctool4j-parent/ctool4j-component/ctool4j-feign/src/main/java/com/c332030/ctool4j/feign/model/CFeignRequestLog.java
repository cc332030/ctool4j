package com.c332030.ctool4j.feign.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CFeignRequestLog
 * </p>
 *
 * @since 2026/1/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CFeignRequestLog {

    Long startMills;

    StringBuilder httpLog;

}
