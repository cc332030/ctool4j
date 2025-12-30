package com.c332030.ctool4j.spring.util;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * <p>
 * Description: CAutowiredUtils
 * </p>
 *
 * @since 2025/12/23
 */
@CustomLog
@UtilityClass
public class CAutowiredUtils {

    public void autowired(Class<?> type) {
        autowired(type, null);
    }

    public void autowired(Object object) {
        autowired(object.getClass(), object);
    }

    public void autowired(Class<?> type, Object object) {

        getFieldMap(type).values().forEach(field -> {

            if(null == object
                && !CReflectUtils.isStatic(field)
            ) {
                return;
            }

            autowired(type, object, field);

        });

    }

    @SneakyThrows
    public void autowired(Class<?> type, Object object, Field field) {

        val fieldType = field.getType();
        val bean = SpringUtil.getBean(fieldType);
        field.set(object, bean);

        log.info("CAutowired {}{}.{}({})",
            () -> null != object ? "(object)" : "",
            type::getSimpleName,
            field::getName,
            fieldType::getSimpleName
        );

    }

    public Map<String, Field> getFieldMap(Class<?> clazz) {
        return CReflectUtils.getFieldMap(clazz,
            e -> CClassUtils.isAnnotationPresent(e, CAutowired.class));
    }

    public <T extends Annotation> void listAnnotatedClassThenDo(
        Class<T> annotationClass,
        CConsumer<Class<Object>> consumer
    ) {

        val basePackages = CCollUtils.concatOne(
            CTool4jConstants.BASE_PACKAGE,
            CSpringUtils.getBasePackages()
        );

        basePackages.forEach(basePackage -> {
            val classes = CClassUtils.listAnnotatedClass(annotationClass, basePackage);
            CCollUtils.forEach(classes, consumer);
        });

    }

}
