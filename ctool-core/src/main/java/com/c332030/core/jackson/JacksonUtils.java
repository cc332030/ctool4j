package com.c332030.core.jackson;

import com.c332030.core.jackson.deserializer.CDateDeserializer;
import com.c332030.core.jackson.deserializer.CInstantDeserializer;
import com.c332030.core.jackson.serializer.CDateSerializer;
import com.c332030.core.jackson.serializer.CInstantSerializer;
import com.c332030.core.jackson.serializer.CLongArraySerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: JacksonUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/5
 */
@UtilityClass
public class JacksonUtils {

    public static final ObjectMapper OBJECT_MAPPER;

    public static final ObjectMapper OBJECT_MAPPER_NON_NULL;

    /**
     * 驼峰会转成下划线
     */
    public static final ObjectMapper OBJECT_MAPPER_SNAKE_CASE;
    static {

        OBJECT_MAPPER = configure(new ObjectMapper());

        val module = new SimpleModule();

        // Long to String，避免前端溢出
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(long.class, ToStringSerializer.instance);
        module.addSerializer(long[].class, CLongArraySerializer.INSTANCE);

        module.addSerializer(Date.class, CDateSerializer.INSTANCE);
        module.addDeserializer(Date.class, CDateDeserializer.INSTANCE);

        module.addSerializer(Instant.class, CInstantSerializer.INSTANCE);
        module.addDeserializer(Instant.class, CInstantDeserializer.INSTANCE);

        OBJECT_MAPPER.registerModule(module);

        // 不打印 null
        // 不序列化 null 值，兼容飞书消息报错
        OBJECT_MAPPER_NON_NULL = OBJECT_MAPPER.copy();
        OBJECT_MAPPER_NON_NULL.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 驼峰转下划线
        OBJECT_MAPPER_SNAKE_CASE = OBJECT_MAPPER.copy();
        OBJECT_MAPPER_SNAKE_CASE.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    }

    public <T extends ObjectMapper> T configure(T objectMapper) {

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

        // 避免 LocalDateTime、LocalDate、LocalTime 反序列化失败
        objectMapper.findAndRegisterModules();

        return objectMapper;
    }

}
