package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.spring.bean.CSpringBeans;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CSpringUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CSpringUtils {

    public String getApplicationName() {
        return CSpringConfigBeans.getSpringApplicationConfig().getName();
    }

    public boolean isCurrentContextEvent(ApplicationEvent event) {

        val source = event.getSource();
        if (!(source instanceof ApplicationContext)) {
            return false;
        }

        return isCurrentContext((ApplicationContext)source);
    }

    public boolean isCurrentContext(ApplicationContext applicationContext) {
        return CSpringBeans.getApplicationContext() == applicationContext;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) CSpringBeans.getApplicationContext().getBean(name);
    }

    public <T> T getBean(Class<T> tClass) {
        return CSpringBeans.getApplicationContext().getBean(tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBeanByResolvableType(Class<?> tClass, Class<?>... classes) {

        val resolvableType = ResolvableType.forClassWithGenerics(
                tClass, classes
        );

        return (T)CSpringBeans.getApplicationContext()
                .getBeanProvider(resolvableType)
                .getIfAvailable();
    }

    @SafeVarargs
    public static <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (val consumer : consumers) {
            consumer.accept(bean);
        }
    }

}
