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

    public void autowired() {

        val annotatedClasses = CClassUtils.listAnnotatedClass(CAutowired.class, "com");

        annotatedClasses.forEach(annotatedClass -> {

            val methods = listSetMethod(annotatedClass);
            methods.forEach((CConsumer<Method>)method -> {

                val paramType = method.getParameterTypes()[0];
                val bean = SpringUtil.getBean(paramType);
                method.invoke(null, bean);

                log.info("class: {} autowire bean: {}",
                    annotatedClass.getName(),
                    paramType.getName()
                );

            });

        });

    }

    List<Method> listSetMethod(Class<?> clazz) {
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
