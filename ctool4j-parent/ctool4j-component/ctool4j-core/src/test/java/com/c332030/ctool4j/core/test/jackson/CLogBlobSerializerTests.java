package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.jackson.CLogBlobSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

/**
 * <p>
 * Description: CLogBlobSerializerTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CLogBlobSerializerTests {

    @Test
    public void serializeBlob() throws Exception {

        StringWriter writer = new StringWriter();
        CLogBlobSerializer serializer = new CLogBlobSerializer();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        serializer.serialize("some-long-content", generator, null);
        generator.close();
        Assertions.assertEquals("\"<BLOB>\"", writer.toString());

    }

    @Test
    public void serializeNullContent() throws Exception {

        StringWriter writer = new StringWriter();
        CLogBlobSerializer serializer = new CLogBlobSerializer();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        serializer.serialize(null, generator, null);
        generator.close();
        Assertions.assertEquals("\"<BLOB>\"", writer.toString());

    }

}
