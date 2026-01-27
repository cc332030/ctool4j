package com.c332030.ctool4j.core.jackson.serializer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.IOException;

/**
 * <p>
 * Description: CLongObjectArraySerializer
 * </p>
 *
 * @author c332030
 * @since 2024/3/27
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLongObjectArraySerializer extends JsonSerializer<Long[]> {

    public static final CLongObjectArraySerializer INSTANCE = new CLongObjectArraySerializer();

    @Override
    public void serialize(Long[] values, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeStartArray();
        for (val value : values) {
            g.writeString(StrUtil.toStringOrNull(value));
        }
        g.writeEndArray();
    }

}
