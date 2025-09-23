package com.c332030.ctool4j.core.jackson.deserializer;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;

/**
 * <p>
 * Description: CInstantDeserializer
 * </p>
 *
 * @author c332030
 * @since 2024/3/27
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CInstantDeserializer extends JsonDeserializer<Instant> {

    public static final CInstantDeserializer INSTANCE = new CInstantDeserializer();

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if(JsonTokenId.ID_STRING == parser.currentTokenId()) {
            try {
                return DateUtil.parse(parser.getText()).toInstant();
            } catch (Exception ignored) {}
        }
        return InstantDeserializer.INSTANT.deserialize(parser, context);
    }

}
