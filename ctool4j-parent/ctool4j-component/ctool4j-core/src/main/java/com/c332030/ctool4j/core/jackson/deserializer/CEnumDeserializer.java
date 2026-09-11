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
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;

/**
 * <p>
 * Description: CEnumDeserializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CEnumDeserializer} 为 Jackson 枚举反序列化器，实现 {@code ContextualDeserializer}：</p>
 * <ul>
 *   <li>按枚举名反序列化（trim 后按名匹配）</li>
 *   <li>空白值（空/空白）返回 null</li>
 *   <li>{@code createContextual} 按字段类型绑定具体枚举类型</li>
 * </ul>
 * <p>提供空实例 {@code EMPTY_INSTANCE}（未绑定枚举类型）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>空白/null 值</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>未知枚举名</td>
 *     <td>抛 IllegalArgumentException（包装为 JsonMappingException）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>枚举字段按名称反序列化，容忍前后空格。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>按枚举名精确匹配（忽略大小写由 CEnumUtils 决定）；空白返回 null 与设计一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>反序列化</b></p>
 * <ul>
 *   <li>值经 {@code StrUtil.trim} 后，空白返回 null；否则 {@code CEnumUtils.nameOf(enumClass, value)} 按枚举名反查。</li>
 *   <li>未知枚举名抛 IllegalArgumentException（Jackson 包装为 JsonMappingException）。</li>
 * </ul>
 * <p><b>上下文绑定</b></p>
 * <ul>
 *   <li>{@code createContextual} 经 {@code CJacksonUtils.getRawClass(property)} 取字段类型，{@code CAssert.isTrue(rawClass.isEnum())}</li>
 *   <li>校验枚举后绑定。</li>
 * </ul>
 *
 * @since 2025/8/11
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public class CEnumDeserializer
        extends JsonDeserializer<Enum<?>>
        implements ContextualDeserializer {

    /**
     * 空实例（未绑定枚举类型）
     */
    public static final CEnumDeserializer EMPTY_INSTANCE = new CEnumDeserializer(null);

    private final Class<Enum<?>> enumClass;

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
