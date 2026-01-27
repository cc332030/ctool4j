package com.c332030.ctool4j.core.jackson.serializer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * <p>
 * Description: CLongArraySerializer
 * </p>
 *
 * @author c332030
 * @since 2024/3/27
 */
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLongArraySerializer extends JsonSerializer<long[]> {

    public static final CLongArraySerializer INSTANCE = new CLongArraySerializer();

    @Override
    public void serialize(long[] values, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeStartArray();
        for (long value : values) {
            g.writeString(StrUtil.toStringOrNull(value));
        }
        g.writeEndArray();
    }

}
