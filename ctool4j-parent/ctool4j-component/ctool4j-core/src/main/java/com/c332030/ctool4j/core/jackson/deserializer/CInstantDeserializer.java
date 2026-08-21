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
 * @author c332030
 * @since 2024/3/27
 * @see doc/design/core/CInstantDeserializer.adoc
 * @see doc/design/core/CInstantDeserializerTests.adoc
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
