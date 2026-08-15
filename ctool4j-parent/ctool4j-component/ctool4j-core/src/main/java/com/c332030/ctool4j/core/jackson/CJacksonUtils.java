package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.core.jackson.deserializer.CDateDeserializer;
import com.c332030.ctool4j.core.jackson.deserializer.CEnumDeserializer;
import com.c332030.ctool4j.core.jackson.deserializer.CInstantDeserializer;
import com.c332030.ctool4j.core.jackson.serializer.CDateSerializer;
import com.c332030.ctool4j.core.jackson.serializer.CInstantSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CJacksonUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/5
 */
@UtilityClass
public class CJacksonUtils {

    /**
     * 默认 ObjectMapper
     */
    public final ObjectMapper OBJECT_MAPPER;

    /**
     * 不序列化 null 值的 ObjectMapper
     */
    public final ObjectMapper OBJECT_MAPPER_NON_NULL;

    /**
     * 驼峰会转成下划线
     */
    public final ObjectMapper OBJECT_MAPPER_SNAKE_CASE;

    /**
     * 日志专用 ObjectMapper：不序列化 null 值 + 标注 CLogBlob 的字段输出 &lt;BLOB&gt; 占位符
     * <p>仅用于日志打印链路（CJsonUtils.toJsonLog），全局 ObjectMapper 不带长文本占位符逻辑，
     * 业务序列化需输出真实内容</p>
     */
    public final ObjectMapper OBJECT_MAPPER_LOG;

    /**
     * 自定义序列化/反序列化模块
     */
    private final SimpleModule SIMPLE_MODULE = getDefinedModule();

    public SimpleModule getDefinedModule() {

        val module = new SimpleModule();

        // Long to String，避免前端溢出
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(long.class, ToStringSerializer.instance);

        module.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        module.addSerializer(Date.class, CDateSerializer.INSTANCE);
        module.addDeserializer(Date.class, CDateDeserializer.INSTANCE);

        module.addSerializer(Instant.class, CInstantSerializer.INSTANCE);
        module.addDeserializer(Instant.class, CInstantDeserializer.INSTANCE);

        module.addDeserializer(Enum.class, CEnumDeserializer.EMPTY_INSTANCE);

        // SIMPLE_MODULE 为 private，外部无法通过它注册自定义序列化器污染全局 mapper，无需冻结
        // 注意：makeImmutable() 在低版本 jackson-databind（如 2.13.5）不存在，不可使用
        return module;
    }

    static {

        OBJECT_MAPPER = configure(new ObjectMapper());

        // 不打印 null
        // 不序列化 null 值，兼容飞书消息报错
        OBJECT_MAPPER_NON_NULL = OBJECT_MAPPER.copy();
        OBJECT_MAPPER_NON_NULL.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 驼峰转下划线
        OBJECT_MAPPER_SNAKE_CASE = OBJECT_MAPPER.copy();

        // TODO 低版本不支持
//        OBJECT_MAPPER_SNAKE_CASE.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        OBJECT_MAPPER_SNAKE_CASE.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        // 日志专用：不序列化 null + 长文本字段输出 <BLOB> 占位符（仅日志打印使用，全局 mapper 不带该逻辑）
        OBJECT_MAPPER_LOG = OBJECT_MAPPER_NON_NULL.copy();
        OBJECT_MAPPER_LOG.setSerializerFactory(
            OBJECT_MAPPER_LOG.getSerializerFactory().withSerializerModifier(new CLogBlobSerializerModifier())
        );

    }

    /**
     * 配置 ObjectMapper
     *
     * @param objectMapper 待配置的 ObjectMapper
     * @param <T>          ObjectMapper 类型
     * @return 配置后的 ObjectMapper
     */
    public <T extends ObjectMapper> T configure(T objectMapper) {

        // 避免 LocalDateTime、LocalDate、LocalTime 反序列化失败
        // 需要在 SIMPLE_MODULE 之前注册，否则 JavaTimeModule 的序列化器会覆盖自定义的序列化器
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(SIMPLE_MODULE);

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 长文本字段占位符（CLogBlob）仅注册到日志专用 OBJECT_MAPPER_LOG，不污染全局 mapper
        // json5
        // 字段名不加引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 尾随逗号
        objectMapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        // 单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许反斜杠转义任何字符
        objectMapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        // java 注释
        objectMapper.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);
        // yaml 注释
        objectMapper.configure(JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature(), true);
        // TODO 点开头的小数，低版本不支持
//        objectMapper.configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature(), true);

        return objectMapper;
    }

    /**
     * 反序列化获取字段类型
     * @param property 字段属性
     * @return 字段类型
     */
    public Class<?> getRawClass(BeanProperty property) {

        val type = property.getType();
        if(!type.isCollectionLikeType()) {
            return type.getRawClass();
        }

        return type.getContentType().getRawClass();
    }

}
