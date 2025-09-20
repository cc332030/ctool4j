package com.c332030.ctool.core.jackson.serializer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

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

        val str = Opt.ofNullable(value)
                .map(Date::from)
                .map(DateUtil::formatDateTime)
                .orElse(null);

        gen.writeString(str);

    }

}
