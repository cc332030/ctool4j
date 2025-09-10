package com.c332030.core.jackson.serializer;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Description: CDateSerializer
 * </p>
 *
 * @since 2025/4/14
 */
public class CDateSerializer extends JsonSerializer<Date> {

    public static final CDateSerializer INSTANCE = new CDateSerializer();

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DateUtil.formatDateTime(value));
    }
}
