package com.c332030.ctool4j.core.jackson.serializer;

import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;

/**
 * <p>
 * Description: CInstantSerializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CInstantSerializer} 为 Jackson {@code Instant} 序列化器，扩展 {@code JsonSerializer&lt;Instant&gt;}，将瞬时时间序列化为 日期时间字符串（{@code yyyy-MM-dd HH:mm:ss}）。提供单例 {@code INSTANCE}（私有构造）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要 Instant 统一序列化为日期时间字符串的 JSON 输出。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>固定 {@code yyyy-MM-dd HH:mm:ss} 格式。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>序列化</b></p>
 * <ul>
 *   <li>{@code serialize} 经 {@code CDateUtils.formatDateTime(value)} 输出日期时间字符串。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/7/24
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CInstantSerializer extends JsonSerializer<Instant> {

    /**
     * 单例实例
     */
    public static final CInstantSerializer INSTANCE = new CInstantSerializer();

    /**
     * 序列化为日期时间字符串
     *
     * @param value   瞬时时间
     * @param gen     生成器
     * @param provider 序列化提供者
     */
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(CDateUtils.formatDateTime(value));
    }

}
