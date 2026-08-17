package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.val;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: 日志字段序列化修改器抽象基类：检测标注指定注解的字段，序列化时替换为对应序列化器
 * </p>
 * <p>仅注册到日志专用 ObjectMapper（CJacksonUtils.OBJECT_MAPPER_LOG / CJsonUtils.toJsonLog），
 * 日志打印链路统一生效；全局 ObjectMapper 不注册，业务序列化输出真实内容</p>
 * <p>子类只需声明注解类型与序列化器创建逻辑，字段扫描与替换逻辑由基类统一提供</p>
 *
 * @param <A> 注解类型
 * @since 2026/8/16
 */
public abstract class CLogFieldSerializerModifier<A extends Annotation> extends BeanSerializerModifier {

    /**
     * 注解类型
     */
    private final Class<A> annotationType;

    /**
     * 标注了注解的字段名集合缓存：按类缓存，避免每次序列化重复构建
     */
    private final CClassValue<Set<String>> annotatedFieldNameCache;

    /**
     * 构建修改器
     *
     * @param annotationType 注解类型
     */
    protected CLogFieldSerializerModifier(Class<A> annotationType) {
        this.annotationType = annotationType;
        this.annotatedFieldNameCache = CClassValue.of(type -> getAnnotatedFieldNames(type, annotationType));
    }

    /**
     * 获取标注了注解的字段名集合
     * <p>复用 {@link CReflectUtils#FIELD_MAP_CLASS_VALUE}（内部按类缓存，避免反射扫描）：
     * 递归父类字段，基类字段上的注解对子类序列化同样生效；
     * 子类同名字段覆盖父类字段，仅父类标注且被子类同名遮蔽时该字段不生效（已知取舍）</p>
     *
     * @param type 目标类
     * @return 标注了注解的字段名集合（不可变）
     */
    private static <A extends Annotation> Set<String> getAnnotatedFieldNames(Class<?> type, Class<A> annotationType) {
        return Collections.unmodifiableSet(
            CReflectUtils.FIELD_MAP_CLASS_VALUE.get(type)
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAnnotationPresent(annotationType))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet())
        );
    }

    /**
     * 获取字段上的注解实例
     *
     * @param type          目标类
     * @param fieldName     字段名
     * @param annotationType 注解类型
     * @return 字段上的注解实例，字段不存在或未标注时为 null
     */
    private static <A extends Annotation> A getFieldAnnotation(Class<?> type, String fieldName, Class<A> annotationType) {
        val field = CReflectUtils.FIELD_MAP_CLASS_VALUE.get(type).get(fieldName);
        return null == field ? null : field.getAnnotation(annotationType);
    }

    /**
     * 根据注解创建字段的序列化器
     *
     * @param annotation 字段上的注解实例（getter 注解优先，字段注解兜底）
     * @return 序列化器
     */
    protected abstract JsonSerializer<Object> createSerializer(A annotation);

    /**
     * 修改序列化属性：将标注注解的字段替换为对应序列化器
     *
     * @param config        序列化配置
     * @param beanDesc      目标 bean 描述
     * @param beanProperties 原始属性列表
     * @return 处理后的属性列表（标注了注解的字段替换为对应序列化器）
     */
    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties
    ) {
        // 兼容 getter 上的注解（BeanPropertyWriter 的 primary member 为 getter 时）
        val annotatedFields = annotatedFieldNameCache.get(beanDesc.getBeanClass());
        if (annotatedFields.isEmpty()) {
            return beanProperties;
        }
        beanProperties.forEach(property -> {
            // 兼容 getter 上的注解 + 兜底字段注解（lombok @Data 字段 private 时 primary member 是 getter）
            A annotation = property.getAnnotation(annotationType);
            if (null == annotation && annotatedFields.contains(property.getName())) {
                annotation = getFieldAnnotation(beanDesc.getBeanClass(), property.getName(), annotationType);
            }
            if (null != annotation) {
                property.assignSerializer(createSerializer(annotation));
            }
        });
        return beanProperties;
    }

}
