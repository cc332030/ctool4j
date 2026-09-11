package com.c332030.ctool4j.spring.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CAutowired
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAutowired}：自动注入注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>标注在类或字段上，用于静态/实例字段自动注入</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>CAutowired 自动注入</p>
 * <h2>不适用与边界场景</h2>
 * <p>RUNTIME 可见</p>
 * <h2>已知限制与取舍</h2>
 * <p>RUNTIME 可见</p>
 *
 * @since 2025/12/23
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CAutowired {

}
