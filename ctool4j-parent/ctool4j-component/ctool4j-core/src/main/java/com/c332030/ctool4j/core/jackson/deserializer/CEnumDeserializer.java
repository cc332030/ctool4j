package com.c332030.ctool4j.core.jackson.deserializer;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CEnumUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Getter;
import lombok.val;
import lombok.var;

import java.io.IOException;

/**
 * <p>
 * Description: CEnumDeserializer
 * </p>
 *
 * @since 2025/8/11
 */
@Getter
public class CEnumDeserializer
        extends JsonDeserializer<Enum<?>>
        implements ContextualDeserializer {

    public static final CEnumDeserializer EMPTY_INSTANCE = new CEnumDeserializer(null);

    private final Class<Enum<?>> enumClass;

    public CEnumDeserializer(Class<Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {

        val value = StrUtil.trim(p.getText());
        if(StrUtil.isBlank(value)) {
            return null;
        }

        return CEnumUtils.nameOf(enumClass, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {

        val type = property.getType();
        var rawClass = type.getRawClass();
        if(type.isCollectionLikeType()) {
            rawClass = type.getContentType().getRawClass();
        }

        return new CEnumDeserializer((Class<Enum<?>>) rawClass);
    }

}
