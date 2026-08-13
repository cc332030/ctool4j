package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.core.cache.impl.CRefClassValue;
import com.c332030.ctool4j.definition.annotation.CLogBlob;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.val;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Description: 长文本字段检测：标注 {@link CLogBlob} 的字段序列化时替换为固定占位符
 * </p>
 * <p>注册到 ObjectMapper 后，走 toJson/toJsonNonNull 的日志打印链路统一生效，
 * 一处实现全局复用（dealArgs 参数打印、请求日志 body 等）</p>
 *
 * @since 2026/8/13
 */
public class CLogBlobSerializerModifier extends BeanSerializerModifier {

    /**
     * 标注了 {@link CLogBlob} 的字段名缓存（按类缓存，避免反射扫描）
     */
    private static final CRefClassValue<Set<String>> CLOG_BLOB_FIELD_NAMES = CRefClassValue.of(type -> {
        val blobFields = new HashSet<String>();
        for (val field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(CLogBlob.class)) {
                blobFields.add(field.getName());
            }
        }
        return Collections.unmodifiableSet(blobFields);
    });

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties
    ) {
        // 兼容 getter 上的注解（BeanPropertyWriter 的 primary member 为 getter 时）
        val blobFields = CLOG_BLOB_FIELD_NAMES.get(beanDesc.getBeanClass());
        if (blobFields.isEmpty()) {
            return beanProperties;
        }
        beanProperties.forEach(property -> {
            // 兼容 getter 上的注解 + 兜底字段注解（lombok @Data 字段 private 时 primary member 是 getter）
            if (null != property.getAnnotation(CLogBlob.class)
                || blobFields.contains(property.getName())
            ) {
                property.assignSerializer(CLogBlobSerializer.INSTANCE);
            }
        });
        return beanProperties;
    }

}
