package com.c332030.ctool4j.spring.util;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.spring.bean.CSpringBeans;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CSpringUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CSpringUtils {

    /**
     * 获取当前应用名称
     * @return 应用名称
     */
    public String getApplicationName() {
        return CSpringConfigBeans.getSpringApplicationConfig().getName();
    }

    /**
     * 判断 event 的源是否是当前上下文
     * @param event 事件
     * @return 是否是当前上下文的 event
     */
    public boolean isCurrentContextEvent(ApplicationEvent event) {

        val source = event.getSource();
        if (!(source instanceof ApplicationContext)) {
            return false;
        }

        return isCurrentContext((ApplicationContext)source);
    }

    /**
     * 获取当前应用上下文
     * @return 当前应用上下文
     */
    public ApplicationContext getApplicationContext() {
        return CSpringBeans.getApplicationContext();
    }

    /**
     * 判断 applicationContext 是否是当前上下文
     * @param applicationContext 应用上下文
     * @return 是否是当前上下文
     */
    public boolean isCurrentContext(ApplicationContext applicationContext) {
        return getApplicationContext() == applicationContext;
    }

    /**
     * 获取指定名称的 bean
     * @param name bean 名称
     * @return bean
     * @param <T> 泛型
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T)getApplicationContext().getBean(name);
    }

    /**
     * 获取指定类型的 bean
     * @param tClass bean 类型
     * @return bean
     * @param <T> 泛型
     */
    public <T> T getBean(Class<T> tClass) {
        return getApplicationContext().getBean(tClass);
    }

    /**
     * 获取指定类型的所有 bean
     * @param tClass bean 类型
     * @return bean map
     * @param <T> 泛型
     */
    public <T> Map<String, T> getBeansOfType(Class<T> tClass) {
        return getApplicationContext().getBeansOfType(tClass);
    }

    /**
     * 获取有指定注解的所有 bean
     * @param tClass bean 类型
     * @return bean map
     */
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> tClass) {
        return getApplicationContext().getBeansWithAnnotation(tClass);
    }

    /**
     * 获取类型指定泛型的 bean
     * @param tClass bean 类型
     * @param classes 泛型类型
     * @return bean
     * @param <T> 泛型
     */
    @SuppressWarnings("unchecked")
    public <T> T getBeanByResolvableType(Class<?> tClass, Class<?>... classes) {

        val resolvableType = ResolvableType.forClassWithGenerics(
                tClass, classes
        );

        return (T)getApplicationContext()
                .getBeanProvider(resolvableType)
                .getIfAvailable();
    }

    /**
     * 获取指定类型并注入属性
     * @param tClass bean 类型
     * @param consumers 属性注入
     * @param <T> 泛型
     */
    @SafeVarargs
    public <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (val consumer : consumers) {
            consumer.accept(bean);
        }
    }

    /**
     * 获取指定类型并注入属性
     * @param tClass bean 类型
     * @param classes 需要注入的类
     */
    @SneakyThrows
    public void wireBean(Class<?> tClass, Class<?>... classes) {

        val setMethods = Arrays.stream(classes)
            .map(clazz -> {
                val methods = CReflectUtils.getMethods(clazz)
                    .stream()
                    .filter(CReflectUtils::isStatic)
                    .filter(e -> e.getName().startsWith("set"))
                    .filter(e -> e.getParameterCount() == 1 && e.getParameterTypes()[0] == tClass)
                    .collect(Collectors.toList());

                CAssert.notEmpty(methods, () -> clazz + " 中没有 " + tClass + " 的 set 方法");
                return methods;
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        val bean = getBean(tClass);
        for (val setMethod : setMethods) {
            setMethod.invoke(null, bean);
        }
    }

    public Set<String> getBasePackages() {

        val springApplicationMap = getBeansWithAnnotation(SpringBootApplication.class);
        CAssert.notEmpty(springApplicationMap, "springApplicationMap 不能为空");

        val basePackages = new LinkedHashSet<String>();
        springApplicationMap.values().forEach(springApplication -> {

            val mainApplicationClass = springApplication.getClass();
            CAssert.notNull(mainApplicationClass, "mainApplicationClass 不能为空");

            val springBootAppAnnotation = mainApplicationClass.getAnnotation(SpringBootApplication.class);
            CAssert.notNull(springApplication, "mainApplicationClass 未标识 @SpringBootApplication");

            val scanBasePackages = springBootAppAnnotation.scanBasePackages();
            if(ArrayUtil.isNotEmpty(scanBasePackages)) {

                for (val scanBasePackage : scanBasePackages) {
                    val basePackage = CStrUtils.toAvailable(scanBasePackage);
                    CCollUtils.addIgnoreBlank(basePackages, basePackage);
                }
            } else {
                CCollUtils.addIgnoreBlank(basePackages, mainApplicationClass.getPackage().getName());
            }
        });

        return basePackages;
    }

}
