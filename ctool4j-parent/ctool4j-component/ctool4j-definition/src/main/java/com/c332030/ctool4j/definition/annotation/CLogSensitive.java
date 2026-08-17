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
 * @since 2026/8/16
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
