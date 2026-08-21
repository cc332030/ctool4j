package com.c332030.ctool4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * <p>
 * Description: 长文本字段序列化器，打印时跳过真实内容，输出固定占位符
 * </p>
 *
 * @since 2026/8/13
 * @see "doc/design/core/CLogBlobSerializer.adoc"
 * @see "doc/design/core/CLogBlobSerializerTests.adoc"
 */
public class CLogBlobSerializer extends JsonSerializer<Object> {

    /**
     * 长文本字段打印时的固定占位符
     */
    public static final String BLOB_PLACEHOLDER = "<BLOB>";

    /**
     * 单例实例
     */
    public static final CLogBlobSerializer INSTANCE = new CLogBlobSerializer();

    /**
     * 序列化为固定占位符，避免长文本真实内容出现在日志中
     *
     * @param value       原始值（内容被忽略）
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(BLOB_PLACEHOLDER);
    }

}
