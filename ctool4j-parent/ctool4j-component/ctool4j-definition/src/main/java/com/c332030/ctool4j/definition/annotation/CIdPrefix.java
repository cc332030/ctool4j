package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: IdPrefix
 * </p>
 *
 * @since 2025/1/16
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CIdPrefix {

    String value();

}
