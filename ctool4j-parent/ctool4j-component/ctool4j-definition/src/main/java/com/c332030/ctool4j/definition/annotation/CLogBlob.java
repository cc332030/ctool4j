package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 标识长文本/大对象字段，日志打印时跳过真实内容，输出固定占位符
 * </p>
 * <p>标记在 DTO 字段（或 getter）上，经 CJsonUtils.toJson/toJsonNonNull 序列化时，
 * 该字段值被替换为固定占位符 &lt;BLOB&gt;，避免 base64、文件流等长内容刷屏日志</p>
 *
 * @since 2026/8/13
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogBlob {

}
