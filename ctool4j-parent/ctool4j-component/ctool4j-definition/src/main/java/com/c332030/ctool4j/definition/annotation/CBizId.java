package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CBizId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBizId} 为业务 ID 字段标记注解。</p>
 * <p>字段级注解，{@code value()} 指定业务 ID 字段名（默认空）。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>注解类为声明式标记；常量类为静态常量。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>注解无默认兜底；常量有默认值。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>供业务代码/日志序列化/测试环境判断使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>注解仅声明，实际行为由消费方（如 CJsonUtils、日志序列化器）实现。</li>
 * </ul>
 *
 * @since 2025/12/3
 * @version 1.0
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
