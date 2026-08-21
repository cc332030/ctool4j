package com.c332030.ctool4j.spring.util;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: CAnnotationUtils
 * </p>
 *
 * @since 2026/4/22
 * @see doc/design/spring/CAnnotationUtils.adoc
 * @see doc/design/spring/CAnnotationUtilsTests.adoc
 */
@UtilityClass
public class CAnnotationUtils {

    /**
     * 获取注解的 value 属性值
     *
     * @param metadata       注解元数据
     * @param annotationType 注解类型
     * @param <ANNO>         注解类型
     * @param <VALUE>        属性值类型
     * @return value 属性值；无该注解时返回 null
     */
    public <ANNO extends Annotation, VALUE> VALUE getAnnotationValue(
        AnnotatedTypeMetadata metadata,
        Class<ANNO> annotationType
    ) {
        return getAnnotationAttributeValue(metadata, annotationType, ICValue.VALUE);
    }

    /**
     * 获取注解的指定属性值
     *
     * @param metadata       注解元数据
     * @param annotationType 注解类型
     * @param attributeName  属性名
     * @param <ANNO>         注解类型
     * @param <VALUE>        属性值类型
     * @return 指定属性值；无该注解或属性时返回 null
     */
    @SneakyThrows
    public <ANNO extends Annotation, VALUE> VALUE getAnnotationAttributeValue(
        AnnotatedTypeMetadata metadata,
        Class<ANNO> annotationType,
        String attributeName
    ) {

        val annotationAttributes = metadata.getAnnotationAttributes(annotationType.getName());
        if(MapUtil.isEmpty(annotationAttributes)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        val value = (VALUE) annotationAttributes.get(attributeName);
        return value;
    }

}
