package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.serializer.CInstantSerializer;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CInstantSerializerTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CInstantSerializerTests {

    @Test
    public void serialize() throws Exception {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        Instant instant = date.toInstant();

        StringWriter writer = new StringWriter();
        CInstantSerializer serializer = CInstantSerializer.INSTANCE;
        JsonGenerator generator = new ObjectMapper().getFactory().createGenerator(writer);
        serializer.serialize(instant, generator, null);
        generator.close();
        Assertions.assertEquals("\"2025-03-03 08:01:03\"", writer.toString());

    }

}
