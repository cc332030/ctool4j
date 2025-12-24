package com.c332030.ctool4j.spring.util;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    public void autowired(String packageName) {

        val annotatedClasses = CClassUtils.listAnnotatedClass(CAutowired.class, packageName);
        annotatedClasses.forEach(annotatedClass -> {

            val fields = listAutowiredField(annotatedClass);
            fields.forEach((CConsumer<Field>)field -> {

                val fieldType = field.getType();
                val bean = SpringUtil.getBean(fieldType);
                field.set(null, bean);

                log.info("autowired class: {} field: {}({})",
                    annotatedClass,
                    field.getName(),
                    fieldType
                );

            });

        });

    }

    public List<Field> listAutowiredField(Class<?> clazz) {
        return CReflectUtils.getFieldMapNew(clazz)
            .values()
            .stream()
            .filter(CReflectUtils::isStatic)
            .filter(e -> CClassUtils.isAnnotationPresent(e, CAutowired.class))
            .collect(Collectors.toList());
    }

    public List<Method> listSetMethod(Class<?> clazz) {
        return CReflectUtils.getMethods(clazz)
            .stream()
            .filter(CReflectUtils::isStatic)
            .filter(e -> e.getName().startsWith("set"))
            .filter(e -> e.getParameterCount() == 1)
            .filter(e -> {
                val paramType = e.getParameterTypes()[0];
                return CClassUtils.isAnnotationPresent(paramType, ConfigurationProperties.class);
            })
            .collect(Collectors.toList());
    }

}
