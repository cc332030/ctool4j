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
 */
public class CLogBlobSerializer extends JsonSerializer<Object> {

    /**
     * 长文本字段打印时的固定占位符
     */
    public static final String BLOB_PLACEHOLDER = "<BLOB>";

    public static final CLogBlobSerializer INSTANCE = new CLogBlobSerializer();

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(BLOB_PLACEHOLDER);
    }

}
