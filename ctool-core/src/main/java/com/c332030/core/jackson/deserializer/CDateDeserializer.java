package com.c332030.core.jackson.deserializer;

import cn.hutool.core.date.DateUtil;
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
public class CDateDeserializer extends JsonDeserializer<Date> {

    public static final CDateDeserializer INSTANCE = new CDateDeserializer();

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        switch (p.currentTokenId()) {
            case JsonTokenId.ID_STRING:
                return DateUtil.parse(p.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return new Date(p.getLongValue());
        }

        return null;
    }

}
