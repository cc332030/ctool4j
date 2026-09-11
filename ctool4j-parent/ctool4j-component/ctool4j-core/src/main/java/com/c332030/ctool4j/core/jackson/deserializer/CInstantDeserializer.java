package com.c332030.ctool4j.core.jackson.deserializer;

import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;

/**
 * <p>
 * Description: CInstantDeserializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CInstantDeserializer} 为 Jackson {@code Instant} 反序列化器，扩展 {@code JsonDeserializer&lt;Instant&gt;}，支持：</p>
 * <ul>
 *   <li>字符串（含毫秒时间戳字符串）：{@code CDateUtils.parseInstantMaybeMills}</li>
 *   <li>整型毫秒时间戳：{@code CDateUtils.toInstant}</li>
 *   <li>其他 token：交给默认 {@code InstantDeserializer.INSTANT}</li>
 * </ul>
 * <p>提供单例 {@code INSTANCE}（私有构造）。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要 Instant 字段同时支持字符串与整型毫秒时间戳反序列化。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>null 由默认实现处理（返回 null）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>委托 CDateUtils 统一解析语义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>token 分发</b></p>
 * <ul>
 *   <li>{@code ID_STRING}：字符串经 {@code parseInstantMaybeMills} 智能解析。</li>
 *   <li>{@code ID_NUMBER_INT}：整型毫秒经 {@code toInstant} 转换。</li>
 *   <li>其他 token 回退默认实现。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/27
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CInstantDeserializer extends JsonDeserializer<Instant> {

    /**
     * 单例实例
     */
    public static final CInstantDeserializer INSTANCE = new CInstantDeserializer();

    /**
     * 反序列化瞬时时间：支持字符串（含毫秒）与整型毫秒时间戳，其余 token 交给默认实现
     *
     * @param parser  解析器
     * @param context 反序列化上下文
     * @return 瞬时时间
     */
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_STRING:
                return CDateUtils.parseInstantMaybeMills(parser.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return CDateUtils.toInstant(parser.getLongValue());
        }
        return InstantDeserializer.INSTANT.deserialize(parser, context);
    }

}
