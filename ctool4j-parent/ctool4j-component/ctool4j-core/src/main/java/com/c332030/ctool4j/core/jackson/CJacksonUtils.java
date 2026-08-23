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
 * @see "doc/design/core/CJacksonUtils.adoc"
 * @see "doc/design/core/CJacksonUtilsTests.adoc"
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
     * 日志专用 ObjectMapper：不序列化 null 值 + 标注 CLogBlob 的字段输出 &lt;BLOB&gt; 占位符、
     * 标注 CLogSensitive 的字段脱敏输出
     * <p>仅用于日志打印链路（CJsonUtils.toJsonLog），全局 ObjectMapper 不带占位符/脱敏逻辑，
     * 业务序列化需输出真实内容</p>
     */
    public final ObjectMapper OBJECT_MAPPER_LOG;

    /**
     * 保留原生数字类型的 ObjectMapper：Long/BigDecimal 不序列化为字符串
     * <p>用于 CJsonUtils.toMap 等需要保留数值类型的场景（避免 Long 经 JSON 中转后变 String）；
     * 对外 JSON 输出仍使用 OBJECT_MAPPER（Long 转字符串防前端溢出）。
     * 反序列化时整数统一读为 Long、浮点数统一读为 BigDecimal，保证数值类型与精度稳定。
     * 作为其余 mapper 的构建源头：通用配置（模块注册、feature、json5）仅在此构建一次，
     * 其他 mapper 基于其 copy() 派生并调整差异点，避免多套构建逻辑漂移</p>
     */
    public final ObjectMapper OBJECT_MAPPER_NATIVE;

    /**
     * 自定义序列化/反序列化模块
     */
    private final SimpleModule SIMPLE_MODULE = getDefinedModule();

    /**
     * 构建自定义序列化/反序列化模块
     * <p>Long/BigDecimal 序列化为字符串避免前端溢出，Date/Instant 使用项目统一格式</p>
     *
     * @return 注册了自定义序列化器的模块
     */
    public SimpleModule getDefinedModule() {
        return getDefinedModule(true);
    }

    /**
     * 构建自定义序列化/反序列化模块
     *
     * @param numberToString Long/BigDecimal 是否序列化为字符串（避免前端溢出）
     * @return 注册了自定义序列化器的模块
     */
    public SimpleModule getDefinedModule(boolean numberToString) {

        val module = new SimpleModule();

        if(numberToString) {
            // Long to String，避免前端溢出
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(long.class, ToStringSerializer.instance);

            module.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        }

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

        // 先构建 NATIVE（保留原生数字类型），其余 mapper 均基于其派生：
        // 通用配置（findAndRegisterModules、getDefinedModule、feature、json5）只维护这一处，避免多套构建逻辑漂移
        OBJECT_MAPPER_NATIVE = new ObjectMapper();
        OBJECT_MAPPER_NATIVE.findAndRegisterModules();
        OBJECT_MAPPER_NATIVE.registerModule(getDefinedModule(false));
        configureFeature(OBJECT_MAPPER_NATIVE);
        // 反序列化时整数统一读为 Long、浮点数统一读为 BigDecimal，保证数值类型与精度稳定
        OBJECT_MAPPER_NATIVE.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
        OBJECT_MAPPER_NATIVE.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

        // 默认 mapper：基于 NATIVE 派生，需纠正两处差异——
        // 1) 关闭 NATIVE 特化的数值反序列化，恢复默认（整数 Integer/Long 按值、浮点 Double）
        // 2) 重新注册 Long/BigDecimal -> String 序列化，避免前端溢出
        OBJECT_MAPPER = OBJECT_MAPPER_NATIVE.copy();
        OBJECT_MAPPER.configure(DeserializationFeature.USE_LONG_FOR_INTS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false);
        OBJECT_MAPPER.registerModule(getDefinedModule(true));

        // 不打印 null
        // 不序列化 null 值，兼容飞书消息报错
        OBJECT_MAPPER_NON_NULL = OBJECT_MAPPER.copy();
        OBJECT_MAPPER_NON_NULL.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 驼峰转下划线
        OBJECT_MAPPER_SNAKE_CASE = OBJECT_MAPPER.copy();
        configureSnakeCase(OBJECT_MAPPER_SNAKE_CASE);

        // 日志专用：不序列化 null + 长文本字段输出 <BLOB> 占位符、敏感字段脱敏（仅日志打印使用，全局 mapper 不带该逻辑）
        // copy() 为深拷贝（含 serializerFactory），此处注册 modifier 不会影响源/其他 mapper（有测试覆盖）
        OBJECT_MAPPER_LOG = OBJECT_MAPPER_NON_NULL.copy();
        OBJECT_MAPPER_LOG.setSerializerFactory(
            OBJECT_MAPPER_LOG.getSerializerFactory()
                .withSerializerModifier(new CLogBlobSerializerModifier())
                .withSerializerModifier(new CLogSensitiveSerializerModifier())
        );

    }

    /**
     * 配置驼峰转下划线命名策略
     * <p>高版本才有 PropertyNamingStrategies.SNAKE_CASE，低版本 jackson-databind 不支持，
     * 为兼容低版本只能使用已弃用的 PropertyNamingStrategy.SNAKE_CASE，此弃用警告已知且接受</p>
     */
    @SuppressWarnings("deprecation")
    private static void configureSnakeCase(ObjectMapper objectMapper) {

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
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

        configureFeature(objectMapper);

        return objectMapper;
    }

    /**
     * 配置 ObjectMapper 通用 feature（json5 宽松解析、时间戳关闭等）
     * <p>供 configure 与 OBJECT_MAPPER_NATIVE 复用，避免两种 mapper 的解析行为不一致</p>
     */
    private static void configureFeature(ObjectMapper objectMapper) {

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

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
