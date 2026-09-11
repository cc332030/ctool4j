package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CJsonLog
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CJsonLog} 为JSON 日志标记注解。</p>
 * <p>类型级标记注解，无属性，标注在结果/实体类上。</p>
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
 * @since 2025/9/14
 * @version 1.0
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CJsonLog {

}
