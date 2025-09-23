package com.c332030.ctool4j.core.jackson.deserializer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
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

        try {
            return DateUtil.parse(text);
        } catch (Exception ex) {
            log.debug("strToDate parse text error", ex);
        }

        try {
            if(NumberUtil.isNumber(text)) {
                return longToDate(Long.parseLong(text));
            }
        } catch (Exception ex) {
            log.debug("strToDate parse long error", ex);
        }

        return null;
    }

}
