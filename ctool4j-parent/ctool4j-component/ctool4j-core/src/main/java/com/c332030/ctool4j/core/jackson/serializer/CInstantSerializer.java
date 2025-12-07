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
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CInstantSerializer extends JsonSerializer<Instant> {

    public static final CInstantSerializer INSTANCE = new CInstantSerializer();

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(CDateUtils.formatDateTime(value));
    }

}
