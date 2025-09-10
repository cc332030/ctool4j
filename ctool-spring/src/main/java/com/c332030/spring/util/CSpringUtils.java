package com.c332030.spring.util;

import com.c332030.spring.bean.SpringBeans;
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
        return (T) SpringBeans.getApplicationContext().getBean(name);
    }

    public <T> T getBean(Class<T> tClass) {
        return SpringBeans.getApplicationContext().getBean(tClass);
    }

    @SafeVarargs
    public static <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (Consumer<T> consumer : consumers) {
            consumer.accept(bean);
        }
    }

}
