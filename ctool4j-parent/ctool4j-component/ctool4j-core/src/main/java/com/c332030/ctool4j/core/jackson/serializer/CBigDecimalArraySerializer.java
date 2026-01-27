package com.c332030.ctool4j.core.jackson.serializer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * <p>
 * Description: CBigDecimalArraySerializer
 * </p>
 *
 * @author c332030
 * @since 2024/3/27
 */
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CBigDecimalArraySerializer extends JsonSerializer<BigDecimal[]> {

    public static final CBigDecimalArraySerializer INSTANCE = new CBigDecimalArraySerializer();

    @Override
    public void serialize(BigDecimal[] values, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeStartArray();
        for (val value : values) {
            g.writeString(StrUtil.toStringOrNull(value));
        }
        g.writeEndArray();
    }

}
