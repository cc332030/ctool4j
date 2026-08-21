package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.serializer.CDateSerializer;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Date;

/**
 * <p>
 * Description: CDateSerializerTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CDateSerializerTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void serialize() throws Exception {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");

        StringWriter writer = new StringWriter();
        CDateSerializer serializer = new CDateSerializer();
        JsonGenerator generator = new ObjectMapper().getFactory().createGenerator(writer);
        serializer.serialize(date, generator, null);
        generator.close();
        Assertions.assertEquals("\"2025-03-03 08:01:03\"", writer.toString());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void instanceNotNull() {

        Assertions.assertNotNull(CDateSerializer.INSTANCE);

    }

}
