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

    /**
     * 注入指定类的全部静态 CAutowired 字段
     *
     * @param type 类
     */
    public void autowired(Class<?> type) {
        autowired(type, null);
    }

    /**
     * 注入对象的全部 CAutowired 字段
     *
     * @param object 对象
     */
    public void autowired(Object object) {
        autowired(object.getClass(), object);
    }

    /**
     * 注入指定类或对象的 CAutowired 字段，静态字段以类注入，实例字段以对象注入
     *
     * @param type   类
     * @param object 对象，为 null 时仅注入静态字段
     */
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

    /**
     * 注入单个字段
     *
     * @param type   类
     * @param object 对象，为 null 时表示静态字段
     * @param field  字段
     */
    @SneakyThrows
    public void autowired(Class<?> type, Object object, Field field) {

        val fieldType = field.getType();
        val bean = SpringUtil.getBean(fieldType);
        field.set(object, bean);

        log.debug("CAutowired {}{}.{}({})",
            () -> null != object ? "(object)" : "",
            type::getSimpleName,
            field::getName,
            fieldType::getSimpleName
        );

    }

    /**
     * 获取标注了 CAutowired 注解的字段映射
     *
     * @param clazz 类
     * @return 字段名与字段的映射
     */
    public Map<String, Field> getFieldMap(Class<?> clazz) {
        return CReflectUtils.getFieldMap(clazz,
            e -> CClassUtils.isAnnotationPresent(e, CAutowired.class));
    }

    /**
     * 扫描并处理标注指定注解的类
     *
     * @param annotationClass 注解类型
     * @param consumer        处理回调
     * @param <T>             注解类型
     */
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
