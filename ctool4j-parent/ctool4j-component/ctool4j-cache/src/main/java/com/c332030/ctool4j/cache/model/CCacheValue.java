package com.c332030.ctool4j.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Description: CCacheValue
 * </p>
 *
 * @since 2026/6/16
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CCacheValue<T> {

    T value;

}
