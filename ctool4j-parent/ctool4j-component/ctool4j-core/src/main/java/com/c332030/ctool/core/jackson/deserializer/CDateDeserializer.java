package com.c332030.ctool.core.jackson.deserializer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

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
                return strToDate(p.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return longToDate(p.getLongValue());
        }

        return null;
    }

    private static Date longToDate(long time) {
        return new Date(time);
    }

    private static Date strToDate(String text) {

        if(StrUtil.isEmpty(text)) {
            return null;
        }

        if(NumberUtil.isNumber(text)) {
            return longToDate(Long.parseLong(text));
        }

        return DateUtil.parse(text);
    }

}
