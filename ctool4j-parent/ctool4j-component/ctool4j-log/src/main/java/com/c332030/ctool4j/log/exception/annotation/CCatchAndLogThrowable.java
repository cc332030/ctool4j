package com.c332030.ctool4j.log.exception.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: CCatchAndLogThrowable
 * </p>
 *
 * @since 2025/12/21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CCatchAndLogThrowable {

}
