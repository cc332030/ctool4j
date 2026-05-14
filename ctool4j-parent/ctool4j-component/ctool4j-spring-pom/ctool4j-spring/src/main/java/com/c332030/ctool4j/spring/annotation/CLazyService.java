package com.c332030.ctool4j.spring.annotation;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CLazyService
 * </p>
 *
 * @since 2026/5/14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component

@Lazy
@Service
public @interface CLazyService {

    @AliasFor(annotation = Service.class)
    String value() default "";

}
