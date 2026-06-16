package com.c332030.ctool4j.cache.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CCacheValue
 * </p>
 *
 * @since 2026/6/16
 */
@Data
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CCacheValue<T> {

    T value;

    Long createMills;

}
