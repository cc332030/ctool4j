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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CBizId {

    /**
     * 业务 ID 字段名，默认为空
     *
     * @return 业务 ID 字段名
     */
    String value() default "";

}
