package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.spring.bean.CSpringBeans;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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
    public static <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (val consumer : consumers) {
            consumer.accept(bean);
        }
    }

    /**
     * 获取指定类型并注入属性
     * @param tClass bean 类型
     * @param classes 需要注入的类
     * @param <T> 泛型
     */
    @SafeVarargs
    @SneakyThrows
    public static <T> void wireBean(Class<T> tClass, Class<T>... classes) {

        val setMethods = Arrays.stream(classes)
            .map(clazz -> {
                val methods = CReflectUtils.getMethods(clazz)
                    .stream()
                    .filter(CReflectUtils::isStatic)
                    .filter(e -> e.getName().startsWith("set"))
                    .filter(e -> e.getParameterCount() == 1 && e.getParameterTypes()[0] == tClass)
                    .collect(Collectors.toList());

                CAssert.notEmpty(methods, () -> "没有找到注入类的 set 方法，tClass：" +  clazz);
                return methods;
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        val bean = getBean(tClass);
        for (val setMethod : setMethods) {
            setMethod.invoke(null, bean);
        }
    }

}
