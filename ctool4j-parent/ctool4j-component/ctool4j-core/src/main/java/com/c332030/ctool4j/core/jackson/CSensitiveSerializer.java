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
 * @since 2026/8/16
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
