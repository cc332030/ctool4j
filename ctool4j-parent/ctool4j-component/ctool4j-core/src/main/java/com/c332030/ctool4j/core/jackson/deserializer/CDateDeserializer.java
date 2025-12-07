package com.c332030.ctool4j.core.jackson.deserializer;

import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.CustomLog;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Description: CDateDeserializer
 * </p>
 *
 * @since 2025/4/14
 */
@CustomLog
public class CDateDeserializer extends JsonDeserializer<Date> {

    public static final CDateDeserializer INSTANCE = new CDateDeserializer();

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_STRING:
                return CDateUtils.parseMaybeMills(parser.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return CDateUtils.toDate(parser.getLongValue());
        }
        return null;
    }

}
