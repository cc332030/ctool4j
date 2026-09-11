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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证序列化输出固定占位符，覆盖非空内容与 null 内容两分支。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对输出 {@code &lt;BLOB&gt;} 占位符的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：非空内容输出 {@code &lt;BLOB&gt;}；null 内容同样输出 {@code &lt;BLOB&gt;}。</li>
 *   <li>未覆盖：无（覆盖了核心行为）。</li>
 * </ul>
 * <h2>序列化</h2>
 * <ul>
 *   <li>1.1 非空内容：输出 {@code "&lt;BLOB&gt;"}（serializeBlob）</li>
 *   <li>1.2 null 内容：输出 {@code "&lt;BLOB&gt;"}（serializeNullContent）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CLogBlobSerializerTests {

    /**
     * 对应测试用例 1.1：非空内容：输出 {@code "&lt;BLOB&gt;"}
     */
    @Test
    public void serializeBlob() throws Exception {

        StringWriter writer = new StringWriter();
        CLogBlobSerializer serializer = new CLogBlobSerializer();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        serializer.serialize("some-long-content", generator, null);
        generator.close();
        Assertions.assertEquals("\"<BLOB>\"", writer.toString());

    }

    /**
     * 对应测试用例 1.2：null 内容：输出 {@code "&lt;BLOB&gt;"}
     */
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
