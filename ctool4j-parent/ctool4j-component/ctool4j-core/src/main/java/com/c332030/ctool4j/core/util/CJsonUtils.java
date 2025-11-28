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

    @SneakyThrows
    public String toJson(Object object, ObjectMapper objectMapper){
        if(null == object) {
            return null;
        }
        return objectMapper.writeValueAsString(object);
    }

    public String toJson(Object object){
        if(null == object) {
            return null;
        }
        return toJson(object, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 驼峰会转成下划线
     */
    public String toJsonSnakeCase(Object object){
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    public String toJsonNonNull(Object object) {
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
    }

    @SneakyThrows
    public <T> T fromJson(String json, Class<T> tClass, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, tClass);
    }

    @SneakyThrows
    public <T> T fromJson(String json, TypeReference<T> typeReference, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }

    public <T> T fromJson(String json, Class<T> tClass) {
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER);
    }
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER);
    }

    public <T> T fromJsonSnakeCase(String json, Class<T> tClass){
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }
    public <T> T fromJsonSnakeCase(String json, TypeReference<T> typeReference){
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }


    /**
     * 转换，如 bean to map，map to bean
     */
    public <T> T convert(Object object, TypeReference<T> typeReference) {
        return fromJson(toJson(object), typeReference);
    }
    public <T> T convertSnakeCase(Object object, TypeReference<T> typeReference) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), typeReference);
    }

    public <T> T convert(Object object, Class<T> tClass) {
        return fromJson(toJson(object), tClass);
    }
    public <T> T convertSnakeCase(Object object, Class<T> tClass) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), tClass);
    }

    public Map<String, Object> toMap(Object object) {
        return fromJson(toJson(object));
    }
    public Map<String, Object> toMapSnakeCase(Object object) {
        return fromJson(toJsonSnakeCase(object));
    }
    public Map<String, Object> fromJson(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
    }
    public List<Map<String, Object>> fromJsonList(String json) {
        return fromJson(json, CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    public Map<String, String> toMapStringValue(Object object) {
        return fromJsonStringValue(toJson(object));
    }
    public Map<String, String> toMapStringValueSnakeCase(Object object) {
        return fromJsonStringValue(toJsonSnakeCase(object));
    }
    public Map<String, String> fromJsonStringValue(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_STRING_TYPE_REFERENCE);
    }

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
