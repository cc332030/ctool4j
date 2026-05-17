package com.c332030.ctool4j.web.exception.annotation;

import com.c332030.ctool4j.web.exception.condition.ConditionalOnMissingExceptionHandlerCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p>
 * Description: ConditionalOnMissingExceptionHandler
 * </p>
 *
 * @since 2026/4/22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ConditionalOnMissingExceptionHandlerCondition.class)
public @interface ConditionalOnMissingExceptionHandler {

    /**
     * The exception type to check.
     *
     * @return the exception class
     */
    Class<? extends Throwable> value();

}
