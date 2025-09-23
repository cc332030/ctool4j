package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.spring.bean.CSpringBeans;
import lombok.experimental.UtilityClass;
import lombok.val;

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

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) CSpringBeans.getApplicationContext().getBean(name);
    }

    public <T> T getBean(Class<T> tClass) {
        return CSpringBeans.getApplicationContext().getBean(tClass);
    }

    @SafeVarargs
    public static <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (Consumer<T> consumer : consumers) {
            consumer.accept(bean);
        }
    }

}
