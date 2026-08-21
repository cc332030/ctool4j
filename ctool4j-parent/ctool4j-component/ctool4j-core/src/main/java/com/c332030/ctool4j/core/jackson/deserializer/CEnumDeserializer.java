package com.c332030.ctool4j.core.jackson.deserializer;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CEnumUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Getter;
import lombok.val;

import java.io.IOException;

/**
 * <p>
 * Description: CEnumDeserializer
 * </p>
 *
 * @since 2025/8/11
 * @see doc/design/core/CEnumDeserializer.adoc
 * @see doc/design/core/CEnumDeserializerTests.adoc
 */
@Getter
public class CEnumDeserializer
        extends JsonDeserializer<Enum<?>>
        implements ContextualDeserializer {

    /**
     * 空实例（未绑定枚举类型）
     */
    public static final CEnumDeserializer EMPTY_INSTANCE = new CEnumDeserializer(null);

    private final Class<Enum<?>> enumClass;

    /**
     * 构造枚举反序列化器
     *
     * @param enumClass 枚举类型
     */
    public CEnumDeserializer(Class<Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * 按枚举名反序列化，空白值返回 null
     *
     * @param p       解析器
     * @param context 反序列化上下文
     * @return 枚举实例
     */
    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {

        val value = StrUtil.trim(p.getText());
        if(StrUtil.isBlank(value)) {
            return null;
        }

        return CEnumUtils.nameOf(enumClass, value);
    }

    /**
     * 按字段类型创建绑定具体枚举类型的反序列化器
     *
     * @param context  反序列化上下文
     * @param property 字段属性
     * @return 绑定枚举类型的反序列化器
     */
    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {

        val rawClass = CJacksonUtils.getRawClass(property);
        CAssert.isTrue(rawClass.isEnum(), () -> "rawClass is not enum: " + rawClass);
        return new CEnumDeserializer((Class<Enum<?>>) rawClass);
    }

}
