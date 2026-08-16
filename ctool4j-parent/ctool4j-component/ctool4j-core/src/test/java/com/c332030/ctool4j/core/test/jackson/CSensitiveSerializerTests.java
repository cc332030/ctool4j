package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.jackson.CSensitiveSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

/**
 * <p>
 * Description: CSensitiveSerializerTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CSensitiveSerializerTests {

    @Test
    public void maskDefaultKeepPrefix3Suffix4() {

        // 11 位手机号：前 3 后 4，中间 11-3-4=4 个 *
        Assertions.assertEquals("138****5678", new CSensitiveSerializer().mask("13812345678"));
        Assertions.assertEquals("abc****hijk", new CSensitiveSerializer().mask("abcdefghijk"));

    }

    @Test
    public void maskCustomKeep() {

        Assertions.assertEquals("1*****7", new CSensitiveSerializer(1, 1).mask("1234567"));
        Assertions.assertEquals("****5678", new CSensitiveSerializer(0, 4).mask("12345678"));

    }

    @Test
    public void maskNull() {

        Assertions.assertNull(new CSensitiveSerializer().mask(null));

    }

    @Test
    public void maskEmptyString() {

        Assertions.assertEquals("", new CSensitiveSerializer().mask(""));

    }

    @Test
    public void maskShortValueAllMasked() {

        // 长度不足以同时保留前后缀时全部打码（安全优先）
        Assertions.assertEquals("*******", new CSensitiveSerializer().mask("1234567"));
        Assertions.assertEquals("***", new CSensitiveSerializer().mask("abc"));

    }

    @Test
    public void maskArbitraryIllegalInput() {

        // 异常输入不限定范围：笔误值、随意捏造值均按字符串脱敏处理
        Assertions.assertEquals("txs******hing", new CSensitiveSerializer().mask("txso/anything"));
        // vldeo/mp4 前缀 vld、后缀 /mp4，中间 2 个字符打 2 个 *
        Assertions.assertEquals("vld**/mp4", new CSensitiveSerializer().mask("vldeo/mp4"));
        Assertions.assertEquals("*******", new CSensitiveSerializer().mask("   abcd"));

    }

    @Test
    public void serializeNullContent() throws Exception {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        new CSensitiveSerializer().serialize(null, generator, null);
        generator.close();
        Assertions.assertEquals("null", writer.toString());

    }

    @Test
    public void serializeContent() throws Exception {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        new CSensitiveSerializer().serialize("13812345678", generator, null);
        generator.close();
        Assertions.assertEquals("\"138****5678\"", writer.toString());

    }

}
