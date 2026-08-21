package com.c332030.ctool4j.core.jackson.serializer;

import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;

/**
 * <p>
 * Description: CInstantSerializer
 * </p>
 *
 * @author c332030
 * @since 2024/7/24
 * @see doc/design/core/CInstantSerializer.adoc
 * @see doc/design/core/CInstantSerializerTests.adoc
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CInstantSerializer extends JsonSerializer<Instant> {

    /**
     * 单例实例
     */
    public static final CInstantSerializer INSTANCE = new CInstantSerializer();

    /**
     * 序列化为日期时间字符串
     *
     * @param value   瞬时时间
     * @param gen     生成器
     * @param provider 序列化提供者
     */
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(CDateUtils.formatDateTime(value));
    }

}
