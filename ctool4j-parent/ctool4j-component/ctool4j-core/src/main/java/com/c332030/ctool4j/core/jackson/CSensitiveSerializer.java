package com.c332030.ctool4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.val;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>
 * Description: 敏感字段序列化器，序列化时脱敏：保留前后缀，中间以 {@code *} 填充
 * </p>
 * <p>默认保留前 {@value #DEFAULT_PREFIX_KEEP} 位、后 {@value #DEFAULT_SUFFIX_KEEP} 位；
 * 字符串长度不足以同时保留前后缀时全部打码（安全优先，避免短敏感值泄露）；
 * 空值输出 null</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSensitiveSerializer} 为敏感字段序列化器，扩展 {@code JsonSerializer&lt;Object&gt;}，序列化时脱敏输出。</p>
 * <ul>
 *   <li>默认保留前 {@code DEFAULT_PREFIX_KEEP=3} 位、后 {@code DEFAULT_SUFFIX_KEEP=4} 位，中间以 {@code *} 填充。</li>
 *   <li>支持自定义保留位数构造。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>mask(null)</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>mask(空串)</td>
 *     <td>返回空串</td>
 *   </tr>
 *   <tr>
 *     <td>mask 长度不足保留前后缀</td>
 *     <td>全部打码</td>
 *   </tr>
 *   <tr>
 *     <td>serialize(null)</td>
 *     <td>输出 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>敏感字段（手机号/身份证等）日志/响应脱敏。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>短值全部打码，牺牲部分可读性换取安全。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>脱敏规则</b></p>
 * <ul>
 *   <li>长度 {@code len &lt;= prefixKeep + suffixKeep} 时全部打码（安全优先，避免短敏感值泄露）。</li>
 *   <li>否则保留前 prefixKeep、后 suffixKeep，中间补 {@code *}。</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CSensitiveSerializer extends JsonSerializer<Object> {

    /**
     * 默认保留的前缀字符数
     */
    public static final int DEFAULT_PREFIX_KEEP = 3;

    /**
     * 默认保留的后缀字符数
     */
    public static final int DEFAULT_SUFFIX_KEEP = 4;

    /**
     * 脱敏填充字符
     */
    public static final char MASK_CHAR = '*';

    /**
     * 保留的前缀字符数
     */
    private final int prefixKeep;

    /**
     * 保留的后缀字符数
     */
    private final int suffixKeep;

    /**
     * 构建默认脱敏序列化器（保留前 {@value #DEFAULT_PREFIX_KEEP} 后 {@value #DEFAULT_SUFFIX_KEEP}）
     */
    public CSensitiveSerializer() {
        this(DEFAULT_PREFIX_KEEP, DEFAULT_SUFFIX_KEEP);
    }

    /**
     * 构建指定保留位数的脱敏序列化器
     *
     * @param prefixKeep 保留的前缀字符数
     * @param suffixKeep 保留的后缀字符数
     */
    public CSensitiveSerializer(int prefixKeep, int suffixKeep) {
        this.prefixKeep = prefixKeep;
        this.suffixKeep = suffixKeep;
    }

    /**
     * 序列化时脱敏输出
     *
     * @param value       原始值
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (null == value) {
            gen.writeNull();
            return;
        }
        gen.writeString(mask(value.toString()));
    }

    /**
     * 脱敏：保留前 {@code prefixKeep} 位、后 {@code suffixKeep} 位，中间以 {@code *} 填充
     * <p>长度不足以同时保留前后缀时全部打码；null 返回 null</p>
     * <ul>
     *   <li>{@code mask(value)}：脱敏字符串；长度不足以同时保留前后缀时全部打码（安全优先）；null 返回 null。</li>
     *   <li>value 为 null 输出 {@code null}；否则输出 {@code mask(value.toString())}。</li>
     * </ul>
     *
     * @param value 原始字符串
     * @return 脱敏后的字符串
     */
    public String mask(String value) {
        if (null == value) {
            return null;
        }
        val len = value.length();
        if (len <= prefixKeep + suffixKeep) {
            return repeat(MASK_CHAR, len);
        }
        return value.substring(0, prefixKeep)
            + repeat(MASK_CHAR, len - prefixKeep - suffixKeep)
            + value.substring(len - suffixKeep);
    }

    /**
     * 生成 count 个重复字符
     *
     * @param c     字符
     * @param count 重复次数
     * @return 重复字符组成的字符串，count 不大于 0 时返回空串
     */
    private static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

}
