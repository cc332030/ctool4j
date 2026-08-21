package com.c332030.ctool4j.cache.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheId
 * </p>
 *
 * @see doc/design/cache/CCacheId.adoc
 * @see doc/design/cache/CCacheAspectCacheKeyTests.adoc
 * @since 2026/6/16
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheId {

}
