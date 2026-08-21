package com.c332030.ctool4j.definition.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CStringId
 * </p>
 *
 * @since 2025/5/26
 * @see doc/design/core/CStringId.adoc
 * @see doc/design/core/CStringIdTests.adoc
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CStringId extends CId<String> implements ICStringId {

}
