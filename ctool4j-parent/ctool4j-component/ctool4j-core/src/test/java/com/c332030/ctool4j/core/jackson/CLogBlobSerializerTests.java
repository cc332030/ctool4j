package com.c332030.ctool4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CLogBlobSerializerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「阈值内打印真实内容 / 超阈值输出占位符（附规模）」两个维度，覆盖各值类型与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计：规模不超过阈值打印真实内容；超过时 CharSequence/Collection/Map/数组按类型附规模；规模无法评估输出 &lt;BLOB&gt;。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：字符串阈值内/超阈值/等值边界、集合阈值内/超阈值、Map、byte[]、其他数组、其他对象、null、默认阈值边界。</li>
 *   <li>未覆盖：无（覆盖了全部类型分支与边界）。</li>
 * </ul>
 * <h2>规模阈值与占位符</h2>
 * <ul>
 *   <li>1.1 字符串 ≤ 阈值：打印真实内容（charSequenceWithinThresholdPrintsContent）</li>
 *   <li>1.2 字符串 &gt; 阈值：输出 {@code "&lt;BLOB:chars=17&gt;"}（charSequenceOverThresholdPlaceholder）</li>
 *   <li>1.3 字符串 = 阈值（边界含）：打印真实内容（charSequenceEqualsThresholdPrintsContent）</li>
 *   <li>1.4 集合 ≤ 阈值：打印真实内容（collectionWithinThresholdPrintsContent）</li>
 *   <li>1.5 集合 &gt; 阈值：输出 {@code "&lt;BLOB:list=3&gt;"}（collectionOverThresholdPlaceholder）</li>
 *   <li>1.6 Map &gt; 阈值：输出 {@code "&lt;BLOB:map=2&gt;"}（mapOverThresholdPlaceholder）</li>
 *   <li>1.7 byte[] &gt; 阈值：输出 {@code "&lt;BLOB:bytes=5&gt;"}（byteArrayOverThresholdPlaceholder）</li>
 *   <li>1.8 其他数组 &gt; 阈值：输出 {@code "&lt;BLOB:array=2&gt;"}（objectArrayOverThresholdPlaceholder）</li>
 *   <li>1.9 其他对象（规模无法评估）：输出 {@code "&lt;BLOB&gt;"}（otherObjectPlaceholder）</li>
 *   <li>1.10 null：输出 {@code "&lt;BLOB&gt;"}（nullPlaceholder）</li>
 *   <li>1.11 默认阈值 10：10 字符打印真实内容、11 字符占位（defaultMaxSizeBoundary）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.3
 */
public class CLogBlobSerializerTests {

    /**
     * 对应测试用例 1.1：字符串 ≤ 阈值：打印真实内容
     */
    @Test
    public void charSequenceWithinThresholdPrintsContent() throws Exception {
        Assertions.assertEquals("\"some\"", serialize("some", 10));
    }

    /**
     * 对应测试用例 1.2：字符串 &gt; 阈值：输出 {@code "&lt;BLOB:chars=17&gt;"}
     */
    @Test
    public void charSequenceOverThresholdPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB:chars=17>\"", serialize("some-long-content", 10));
    }

    /**
     * 对应测试用例 1.3：字符串 = 阈值（边界含）：打印真实内容
     */
    @Test
    public void charSequenceEqualsThresholdPrintsContent() throws Exception {
        Assertions.assertEquals("\"0123456789\"", serialize("0123456789", 10));
    }

    /**
     * 对应测试用例 1.4：集合 ≤ 阈值：打印真实内容（保持数组形状）
     */
    @Test
    public void collectionWithinThresholdPrintsContent() throws Exception {
        Assertions.assertEquals("[\"a\",\"b\"]", serialize(Arrays.asList("a", "b"), 2));
    }

    /**
     * 对应测试用例 1.5：集合 &gt; 阈值：输出 {@code "&lt;BLOB:list=3&gt;"}
     */
    @Test
    public void collectionOverThresholdPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB:list=3>\"", serialize(Arrays.asList("a", "b", "c"), 2));
    }

    /**
     * 对应测试用例 1.6：Map &gt; 阈值：输出 {@code "&lt;BLOB:map=2&gt;"}
     */
    @Test
    public void mapOverThresholdPlaceholder() throws Exception {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        Assertions.assertEquals("\"<BLOB:map=2>\"", serialize(map, 1));
    }

    /**
     * 对应测试用例 1.7：byte[] &gt; 阈值：输出 {@code "&lt;BLOB:bytes=5&gt;"}
     */
    @Test
    public void byteArrayOverThresholdPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB:bytes=5>\"", serialize(new byte[5], 4));
    }

    /**
     * 对应测试用例 1.8：其他数组 &gt; 阈值：输出 {@code "&lt;BLOB:array=2&gt;"}
     */
    @Test
    public void objectArrayOverThresholdPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB:array=2>\"", serialize(new Object[] {"a", 1}, 1));
    }

    /**
     * 对应测试用例 1.9：其他对象（规模无法评估）：输出 {@code "&lt;BLOB&gt;"}
     */
    @Test
    public void otherObjectPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB>\"", serialize(new Object(), 10));
    }

    /**
     * 对应测试用例 1.10：null：输出 {@code "&lt;BLOB&gt;"}
     */
    @Test
    public void nullPlaceholder() throws Exception {
        Assertions.assertEquals("\"<BLOB>\"", serialize(null, 10));
    }

    /**
     * 对应测试用例 1.11：默认阈值 10：10 字符打印真实内容、11 字符占位
     */
    @Test
    public void defaultMaxSizeBoundary() throws Exception {

        Assertions.assertEquals("\"" + repeat('a', 10) + "\"", serialize(repeat('a', 10)));
        Assertions.assertEquals("\"<BLOB:chars=11>\"", serialize(repeat('a', 11)));
    }

    /**
     * 经指定阈值序列化器序列化值，返回 JSON 文本
     *
     * @param value   待序列化值
     * @param maxSize 规模阈值
     * @return JSON 文本
     */
    private static String serialize(Object value, int maxSize) throws Exception {
        return serialize(new CLogBlobSerializer(maxSize), value);
    }

    /**
     * 经默认阈值序列化器序列化值，返回 JSON 文本
     *
     * @param value 待序列化值
     * @return JSON 文本
     */
    private static String serialize(Object value) throws Exception {
        return serialize(new CLogBlobSerializer(), value);
    }

    /**
     * 经序列化器序列化值，返回 JSON 文本
     *
     * @param serializer 序列化器
     * @param value      待序列化值
     * @return JSON 文本
     */
    private static String serialize(CLogBlobSerializer serializer, Object value) throws Exception {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = CJacksonUtils.OBJECT_MAPPER.getFactory().createGenerator(writer);
        // 阈值内分支需委托 SerializerProvider 输出真实内容，故传入真实 provider（非 null）
        serializer.serialize(value, generator, CJacksonUtils.OBJECT_MAPPER.getSerializerProviderInstance());
        generator.close();

        return writer.toString();
    }

    /**
     * 生成重复字符的字符串
     *
     * @param c     字符
     * @param count 重复次数
     * @return 字符串
     */
    private static String repeat(char c, int count) {

        char[] chars = new char[count];
        Arrays.fill(chars, c);

        return new String(chars);
    }

}
