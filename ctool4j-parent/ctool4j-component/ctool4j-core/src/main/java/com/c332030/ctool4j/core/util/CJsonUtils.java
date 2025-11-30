package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.*;

/**
 * <p>
 * Description: CJsonUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/4
 */
@UtilityClass
public class CJsonUtils {

    public static final List<MediaType> SUPPORT_MEDIA_TYPES = CList.of(
            MediaType.TEXT_HTML,
            MediaType.TEXT_PLAIN,
            MediaType.APPLICATION_JSON,
            CMimeTypeEnum.JSON5.getMimeType()
    );


    /**
     * 转 json
     *
     * @param object 对象
     * @param objectMapper 对象映射器
     * @return json
     */
    @SneakyThrows
    public String toJson(Object object, ObjectMapper objectMapper){
        if(null == object) {
            return null;
        }
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 转 json
     *
     * @param object 源对象
     * @return json
     */
    public String toJson(Object object){
        if(null == object) {
            return null;
        }
        return toJson(object, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 转 json，驼峰会转成下划线
     * @param object 源对象
     * @return json
     */
    public String toJsonSnakeCase(Object object){
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 转 json，null值不参与转换
     * @param object 源对象
     * @return json
     */
    public String toJsonNonNull(Object object) {
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param tClass 目标对象类型
     * @param objectMapper 对象映射器
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    @SneakyThrows
    public <T> T fromJson(String json, Class<T> tClass, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, tClass);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 目标对象类型
     * @param objectMapper 映射器
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    @SneakyThrows
    public <T> T fromJson(String json, TypeReference<T> typeReference, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param tClass 源对象类型
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    public <T> T fromJson(String json, Class<T> tClass) {
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param tClass 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJsonSnakeCase(String json, Class<T> tClass){
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJsonSnakeCase(String json, TypeReference<T> typeReference){
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param typeReference 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convert(Object object, TypeReference<T> typeReference) {
        return fromJson(toJson(object), typeReference);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param typeReference 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convertSnakeCase(Object object, TypeReference<T> typeReference) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), typeReference);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param tClass 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convert(Object object, Class<T> tClass) {
        return fromJson(toJson(object), tClass);
    }
    public <T> T convertSnakeCase(Object object, Class<T> tClass) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), tClass);
    }

    /**
     * 转 map
     * @param object 源对象
     * @return map
     */
    public Map<String, Object> toMap(Object object) {
        return fromJson(toJson(object));
    }

    /**
     * 转 map，驼峰转下划线
     * @param object 源对象
     * @return map
     */
    public Map<String, Object> toMapSnakeCase(Object object) {
        return fromJson(toJsonSnakeCase(object));
    }

    /**
     * 从 json 转为 map，下划线转驼峰
     * @param json json
     * @return map
     */
    public Map<String, Object> fromJson(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    /**
     * 从 json 转为 list map
     * @param json json
     * @return list map
     */
    public List<Map<String, Object>> fromJsonList(String json) {
        return fromJson(json, CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    /**
     * 转 map string value，下划线转驼峰
     * @param object 源对象
     * @return map string value
     */
    public Map<String, String> toMapStringValue(Object object) {
        return fromJsonStringValue(toJson(object));
    }

    /**
     * 转 map string value，驼峰转下划线
     * @param object 源对象
     * @return map string value
     */
    public Map<String, String> toMapStringValueSnakeCase(Object object) {
        return fromJsonStringValue(toJsonSnakeCase(object));
    }

    /**
     * 从 json 转为 map string value
     * @param json json
     * @return map string value
     */
    public Map<String, String> fromJsonStringValue(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_STRING_TYPE_REFERENCE);
    }

    /**
     * 配置消息转换器
     * @param messageConverters 消息转换器
     * @param objectMapper 映射器
     */
    public void configureMessageConverters(Collection<HttpMessageConverter<?>> messageConverters, ObjectMapper objectMapper) {
        messageConverters.stream()
                .filter(e -> e instanceof MappingJackson2HttpMessageConverter)
                .forEach(e -> {
                    MappingJackson2HttpMessageConverter messageConverter = (MappingJackson2HttpMessageConverter) e;
                    messageConverter.setObjectMapper(objectMapper);

                    Set<MediaType> mediaTypes = new HashSet<>(messageConverter.getSupportedMediaTypes());
                    mediaTypes.addAll(CJsonUtils.SUPPORT_MEDIA_TYPES);
                    messageConverter.setSupportedMediaTypes(new ArrayList<>(mediaTypes));
                });
    }

}
