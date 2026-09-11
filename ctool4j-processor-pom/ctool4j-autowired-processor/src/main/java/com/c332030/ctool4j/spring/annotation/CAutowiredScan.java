package com.c332030.ctool4j.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: CAutowiredScan
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAutowiredScan}：自动注入扫描注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>标注在类上，声明该类的 CAutowired 字段需注入</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>CAutowired 扫描入口</p>
 * <h2>不适用与边界场景</h2>
 * <p>SOURCE 保留</p>
 * <h2>已知限制与取舍</h2>
 * <p>SOURCE 保留</p>
 *
 * @since 2025/12/23
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CAutowiredScan {

}
