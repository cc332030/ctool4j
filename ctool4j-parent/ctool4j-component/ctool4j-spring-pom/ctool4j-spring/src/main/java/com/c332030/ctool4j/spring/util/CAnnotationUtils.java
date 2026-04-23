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
 */
@UtilityClass
public class CAnnotationUtils {

    public <ANNO extends Annotation, VALUE> VALUE getAnnotationAttributeValue(
        AnnotatedTypeMetadata metadata,
        Class<ANNO> annotationType
    ) {
        return getAnnotationAttributeValue(metadata, annotationType, ICValue.VALUE);
    }

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
