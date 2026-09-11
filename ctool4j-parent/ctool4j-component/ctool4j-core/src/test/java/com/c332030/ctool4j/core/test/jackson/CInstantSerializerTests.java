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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证序列化输出日期时间字符串。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code yyyy-MM-dd HH:mm:ss} 输出的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：序列化输出 {@code "2025-03-03 08:01:03"}。</li>
 *   <li>未覆盖：无（覆盖了核心行为）。</li>
 * </ul>
 * <h2>序列化</h2>
 * <ul>
 *   <li>1.1 serialize：输出 {@code "2025-03-03 08:01:03"}（serialize）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CInstantSerializerTests {

    /**
     * 对应测试用例 1.1：输出 {@code "2025-03-03 08:01:03"}
     */
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
