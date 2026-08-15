package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.annotation.CLogBlob;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: 长文本字段检测：标注 {@link CLogBlob} 的字段序列化时替换为固定占位符
 * </p>
 * <p>仅注册到日志专用 ObjectMapper（CJacksonUtils.OBJECT_MAPPER_LOG / CJsonUtils.toJsonLog），
 * 日志打印链路统一生效（toLogArgs 参数打印等）；全局 ObjectMapper 不注册，业务序列化输出真实内容</p>
 *
 * @since 2026/8/13
 */
public class CLogBlobSerializerModifier extends BeanSerializerModifier {

    /**
     * 标注 {@link CLogBlob} 的字段名集合缓存：按类缓存，避免每次序列化重复构建
     */
    static final CClassValue<Set<String>> BLOB_FIELD_NAME_CACHE =
        CClassValue.of(CLogBlobSerializerModifier::getBlobFieldNames);

    /**
     * 获取标注了 {@link CLogBlob} 的字段名集合
     * <p>复用 {@link CReflectUtils#FIELD_MAP_CLASS_VALUE}（内部按类缓存，避免反射扫描）：
     * 递归父类字段，基类字段上的注解对子类序列化同样生效；
     * 子类同名字段覆盖父类字段，仅父类标注且被子类同名遮蔽时该字段不生效（已知取舍）</p>
     *
     * @param type 目标类
     * @return 标注了 {@link CLogBlob} 的字段名集合（不可变）
     */
    private static Set<String> getBlobFieldNames(Class<?> type) {
        return Collections.unmodifiableSet(
            CReflectUtils.FIELD_MAP_CLASS_VALUE.get(type)
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAnnotationPresent(CLogBlob.class))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet())
        );
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties
    ) {
        // 兼容 getter 上的注解（BeanPropertyWriter 的 primary member 为 getter 时）
        val blobFields = BLOB_FIELD_NAME_CACHE.get(beanDesc.getBeanClass());
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
