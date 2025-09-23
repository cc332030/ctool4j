package com.c332030.ctool4j.transaction.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CTransactional
 * </p>
 *
 * @since 2025/9/21
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional
public @interface CTransactional {

    @AliasFor(annotation = Transactional.class)
    Propagation propagation() default Propagation.REQUIRED;

    @AliasFor(annotation = Transactional.class)
    Isolation isolation() default Isolation.DEFAULT;

    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default false;

    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] rollbackFor() default Exception.class;

}
