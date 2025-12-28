package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CBizId
 * </p>
 *
 * @since 2025/12/3
 */
@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CBizId {

    String value() default "";

}
