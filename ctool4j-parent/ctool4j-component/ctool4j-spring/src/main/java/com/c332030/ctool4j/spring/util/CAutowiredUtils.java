package com.c332030.ctool4j.spring.util;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.definition.constant.CToolConstants;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        CReflectUtils.getFieldAllMap(type)
            .values()
            .stream()
            .filter(e -> CClassUtils.isAnnotationPresent(e, CAutowired.class))
            .forEach(field -> {

                val isStatic = CReflectUtils.isStatic(field);
                if(null == object
                    && !isStatic
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

    public List<Field> listAutowiredField(Class<?> clazz) {
        return CReflectUtils.getFieldAllMap(clazz)
            .values()
            .stream()
            .filter(CReflectUtils::isStatic)
            .filter(e -> CClassUtils.isAnnotationPresent(e, CAutowired.class))
            .collect(Collectors.toList());
    }

    public <T extends Annotation> void listAnnotatedClassThenDo(
        Class<T> annotationClass,
        CConsumer<Class<Object>> consumer
    ) {

        val basePackages = new ArrayList<String>();
        basePackages.add(CToolConstants.BASE_PACKAGE);
        basePackages.addAll(CSpringUtils.getBasePackages());

        basePackages.forEach(basePackage -> {
            val classes = CClassUtils.listAnnotatedClass(annotationClass, basePackage);
            CCollUtils.forEach(classes, consumer);
        });

    }

}
