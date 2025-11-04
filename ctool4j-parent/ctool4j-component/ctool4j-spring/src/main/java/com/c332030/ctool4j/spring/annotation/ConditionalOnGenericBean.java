package com.c332030.ctool4j.spring.annotation;

import com.c332030.ctool4j.spring.condition.OnGenericBeanCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p>
 * Description: ConditionalOnGenericBean
 * </p>
 *
 * @since 2025/11/4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnGenericBeanCondition.class) // 关联自定义条件实现
public @interface ConditionalOnGenericBean {

    /**
     * @return 原始类型
     */
    Class<?> type();

    /**
     * @return 泛型参数
     */
    Class<?>[] genericTypes();

}
