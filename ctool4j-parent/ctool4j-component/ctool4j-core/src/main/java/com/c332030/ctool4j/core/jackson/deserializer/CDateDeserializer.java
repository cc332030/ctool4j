package com.c332030.ctool4j.core.jackson.deserializer;

import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import lombok.CustomLog;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Description: CDateDeserializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDateDeserializer} 为 Jackson {@code Date} 反序列化器，扩展 {@code JsonDeserializer&lt;Date&gt;}，支持：</p>
 * <ul>
 *   <li>字符串（含毫秒时间戳字符串）：{@code CDateUtils.parseMaybeMills}</li>
 *   <li>整型毫秒时间戳：{@code CDateUtils.toDate}</li>
 *   <li>其他 token：交给默认 {@code DateDeserializers.DateDeserializer.instance}</li>
 * </ul>
 * <p>提供单例 {@code INSTANCE}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要 Date 字段同时支持字符串与整型毫秒时间戳反序列化。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>空字符串/null/缺失字段由默认实现处理（返回 null）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>委托 CDateUtils 统一解析语义，保证字符串/时间戳兼容。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>token 分发</b></p>
 * <ul>
 *   <li>{@code ID_STRING}：字符串经 {@code parseMaybeMills} 智能解析（日期/时间/日期时间/毫秒/秒时间戳）。</li>
 *   <li>{@code ID_NUMBER_INT}：整型毫秒经 {@code toDate} 转换。</li>
 *   <li>其他 token 回退默认实现。</li>
 * </ul>
 *
 * @since 2025/4/14
 * @version 1.0
 */
@CustomLog
public class CDateDeserializer extends JsonDeserializer<Date> {

    /**
     * 单例实例
     */
    public static final CDateDeserializer INSTANCE = new CDateDeserializer();

    /**
     * 反序列化日期：字符串（含毫秒）与整型毫秒时间戳均支持，其余 token 交给默认实现
     *
     * @param parser  解析器
     * @param context 反序列化上下文
     * @return 日期
     */
    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_STRING:
                return CDateUtils.parseMaybeMills(parser.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return CDateUtils.toDate(parser.getLongValue());
        }
        return DateDeserializers.DateDeserializer.instance.deserialize(parser, context);
    }

}
