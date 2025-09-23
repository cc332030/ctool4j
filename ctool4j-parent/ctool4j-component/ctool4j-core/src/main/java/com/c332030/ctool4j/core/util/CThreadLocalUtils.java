package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ObjUtil;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CThreadLocalUtils
 * </p>
 *
 * @since 2025/9/21
 */
@UtilityClass
public class CThreadLocalUtils {

    public <T> T getThenRemove(ThreadLocal<T> threadLocal) {
        try {
            return threadLocal.get();
        } finally {
            threadLocal.remove();
        }
    }

    public <T> T getOrDefault(ThreadLocal<T> threadLocal, T defaultValue) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValue);
    }

    public <T> T getOrDefault(ThreadLocal<T> threadLocal, Supplier<T> defaultValueSupplier) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValueSupplier);
    }

}
