package com.c332030.ctool4j.core.jackson.serializer;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Description: CDateSerializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDateSerializer} 为 Jackson {@code Date} 序列化器，扩展 {@code JsonSerializer&lt;Date&gt;}，将日期序列化为日期时间字符串 （{@code yyyy-MM-dd HH:mm:ss}）。提供单例 {@code INSTANCE}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要 Date 统一序列化为日期时间字符串的 JSON 输出。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>固定 {@code yyyy-MM-dd HH:mm:ss} 格式，需其他格式时自定义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>序列化</b></p>
 * <ul>
 *   <li>{@code serialize} 经 {@code DateUtil.formatDateTime(value)} 输出 {@code yyyy-MM-dd HH:mm:ss} 字符串。</li>
 * </ul>
 *
 * @since 2025/4/14
 * @version 1.0
 */
public class CDateSerializer extends JsonSerializer<Date> {

    /**
     * 单例实例
     */
    public static final CDateSerializer INSTANCE = new CDateSerializer();

    /**
     * 序列化为日期时间字符串
     *
     * @param value       日期
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DateUtil.formatDateTime(value));
    }
}
