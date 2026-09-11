package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 标识敏感字段，日志打印时脱敏，默认保留前三位后四位
 * </p>
 * <p>标记在 DTO 字段（或 getter）上，经 CJsonUtils.toJsonLog（日志专用 mapper）序列化时，
 * 该字段值按保留前缀/后缀位数脱敏，中间以 {@code *} 填充，避免手机号、身份证等敏感信息明文进日志；
 * 全局 ObjectMapper 无该行为，业务序列化输出真实内容</p>
 * <p>保留位数可通过 {@link #prefixKeep()} / {@link #suffixKeep()} 自定义；
 * 长度不足以同时保留前后缀时全部打码（安全优先）</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogSensitive} 为日志脱敏注解。</p>
 * <p>字段/getter 级注解，日志序列化时按保留前后缀位数脱敏（默认前缀3后缀4），中间 {@code *} 填充；长度不足全部打码。</p>
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
 * @since 2026/8/16
 * @version 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogSensitive {

    /**
     * 保留的前缀字符数
     *
     * @return 前缀字符数
     */
    int prefixKeep() default 3;

    /**
     * 保留的后缀字符数
     *
     * @return 后缀字符数
     */
    int suffixKeep() default 4;

}
