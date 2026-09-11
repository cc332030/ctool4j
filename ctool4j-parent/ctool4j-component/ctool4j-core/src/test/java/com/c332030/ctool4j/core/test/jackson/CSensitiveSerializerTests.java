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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「mask 脱敏 / 序列化」两个维度组织。</li>
 *   <li>mask 覆盖默认保留、自定义保留、null、空串、短值全部打码、任意非法输入。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对脱敏规则（保留前后缀、短值全打码）的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认保留前 3 后 4；自定义保留（1,1/0,4）；null；空串；短值全部打码；任意非法输入脱敏；</li>
 *   <li>序列化 null 输出 null；序列化正常脱敏。</li>
 *   <li>未覆盖：无（覆盖了 mask 与序列化核心路径）。</li>
 * </ul>
 * <h2>mask 脱敏</h2>
 * <ul>
 *   <li>1.1 默认保留：前 3 后 4 中间 {@code *}（maskDefaultKeepPrefix3Suffix4）</li>
 *   <li>1.2 自定义保留：自定义 prefixKeep/suffixKeep（maskCustomKeep）</li>
 *   <li>1.3 null：返回 null（maskNull）</li>
 *   <li>1.4 空串：返回空串（maskEmptyString）</li>
 *   <li>1.5 短值：长度不足时全部打码（maskShortValueAllMasked）</li>
 *   <li>1.6 任意非法输入：按字符串脱敏处理（maskArbitraryIllegalInput）</li>
 * </ul>
 * <h2>序列化</h2>
 * <ul>
 *   <li>2.1 null 内容：输出 {@code null}（serializeNullContent）</li>
 *   <li>2.2 正常内容：输出脱敏后字符串（serializeContent）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CSensitiveSerializerTests {

    /**
     * 对应测试用例 1.1：默认保留：前 3 后 4 中间 {@code *}
     */
    @Test
    public void maskDefaultKeepPrefix3Suffix4() {

        // 11 位手机号：前 3 后 4，中间 11-3-4=4 个 *
        Assertions.assertEquals("138****5678", new CSensitiveSerializer().mask("13812345678"));
        Assertions.assertEquals("abc****hijk", new CSensitiveSerializer().mask("abcdefghijk"));

    }

    /**
     * 对应测试用例 1.2：自定义保留：自定义 prefixKeep/suffixKeep
     */
    @Test
    public void maskCustomKeep() {

        Assertions.assertEquals("1*****7", new CSensitiveSerializer(1, 1).mask("1234567"));
        Assertions.assertEquals("****5678", new CSensitiveSerializer(0, 4).mask("12345678"));

    }

    /**
     * 对应测试用例 1.3：返回 null
     */
    @Test
    public void maskNull() {

        Assertions.assertNull(new CSensitiveSerializer().mask(null));

    }

    /**
     * 对应测试用例 1.4：空串：返回空串
     */
    @Test
    public void maskEmptyString() {

        Assertions.assertEquals("", new CSensitiveSerializer().mask(""));

    }

    /**
     * 对应测试用例 1.5：短值：长度不足时全部打码
     */
    @Test
    public void maskShortValueAllMasked() {

        // 长度不足以同时保留前后缀时全部打码（安全优先）
        Assertions.assertEquals("*******", new CSensitiveSerializer().mask("1234567"));
        Assertions.assertEquals("***", new CSensitiveSerializer().mask("abc"));

    }

    /**
     * 对应测试用例 1.6：任意非法输入：按字符串脱敏处理
     */
    @Test
    public void maskArbitraryIllegalInput() {

        // 异常输入不限定范围：笔误值、随意捏造值均按字符串脱敏处理
        Assertions.assertEquals("txs******hing", new CSensitiveSerializer().mask("txso/anything"));
        // vldeo/mp4 前缀 vld、后缀 /mp4，中间 2 个字符打 2 个 *
        Assertions.assertEquals("vld**/mp4", new CSensitiveSerializer().mask("vldeo/mp4"));
        Assertions.assertEquals("*******", new CSensitiveSerializer().mask("   abcd"));

    }

    /**
     * 对应测试用例 2.1：null 内容：输出 {@code null}
     */
    @Test
    public void serializeNullContent() throws Exception {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        new CSensitiveSerializer().serialize(null, generator, null);
        generator.close();
        Assertions.assertEquals("null", writer.toString());

    }

    /**
     * 对应测试用例 2.2：正常内容：输出脱敏后字符串
     */
    @Test
    public void serializeContent() throws Exception {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        new CSensitiveSerializer().serialize("13812345678", generator, null);
        generator.close();
        Assertions.assertEquals("\"138****5678\"", writer.toString());

    }

}
