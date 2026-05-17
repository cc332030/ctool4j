package com.c332030.ctool4j.spring.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CSpringBootApplication
 * </p>
 *
 * @since 2025/11/10
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@SpringBootApplication
@ConfigurationPropertiesScan
public @interface CSpringBootApplication {

    @AliasFor(annotation = SpringBootApplication.class)
    Class<?>[] exclude() default {};

}
