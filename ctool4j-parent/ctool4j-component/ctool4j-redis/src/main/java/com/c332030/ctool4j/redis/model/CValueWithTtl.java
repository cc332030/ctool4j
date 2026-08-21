package com.c332030.ctool4j.redis.model;

import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CValueWithTtl
 * </p>
 *
 * @see "doc/design/redis/CValueWithTtl.adoc"
 * @see "doc/design/redis/CValueWithTtlTests.adoc"
 * @since 2026/3/25
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CValueWithTtl<T> implements ICValue<T> {

    T value;

    Long ttl;

}
