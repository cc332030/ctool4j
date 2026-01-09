package com.c332030.ctool4j.spring.exception.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CLogAndIgnoreThrowable
 * </p>
 *
 * @since 2025/12/21
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogAndIgnoreThrowable {

}
