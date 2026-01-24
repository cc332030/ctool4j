package com.c332030.ctool4j.spring.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CAutowired
 * </p>
 *
 * @since 2025/12/23
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CAutowired {

}
