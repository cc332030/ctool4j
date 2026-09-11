package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 标识长文本/大对象字段，日志打印时跳过真实内容，输出固定占位符
 * </p>
 * <p>标记在 DTO 字段（或 getter）上，经 CJsonUtils.toJsonLog（日志专用 mapper）序列化时，
 * 该字段值被替换为固定占位符 &lt;BLOB&gt;，避免 base64、文件流等长内容刷屏日志；
 * 全局 ObjectMapper 无该行为，业务序列化输出真实内容</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogBlob} 为日志长文本占位注解。</p>
 * <p>字段/getter 级注解，日志序列化时将长内容替换为 {@code &amp;lt;BLOB&amp;gt;} 占位符，避免 base64/文件流刷屏。</p>
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
 * @since 2026/8/13
 * @version 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogBlob {

}
