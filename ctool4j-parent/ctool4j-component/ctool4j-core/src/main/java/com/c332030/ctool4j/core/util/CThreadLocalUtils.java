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
 * @see "doc/design/core/CThreadLocalUtils.adoc"
 * @see "doc/design/core/CThreadLocalUtilsTests.adoc"
 */
@UtilityClass
public class CThreadLocalUtils {

    /**
     * 获取 ThreadLocal 值并立即移除
     *
     * @param threadLocal ThreadLocal
     * @param <T>         值类型
     * @return ThreadLocal 值
     */
    public <T> T getThenRemove(ThreadLocal<T> threadLocal) {
        try {
            return threadLocal.get();
        } finally {
            threadLocal.remove();
        }
    }

    /**
     * 获取 ThreadLocal 值，为 null 时返回默认值
     *
     * @param threadLocal  ThreadLocal
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return ThreadLocal 值或默认值
     */
    public <T> T getOrDefault(ThreadLocal<T> threadLocal, T defaultValue) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValue);
    }

    /**
     * 获取 ThreadLocal 值，为 null 时通过供应商获取默认值
     *
     * @param threadLocal          ThreadLocal
     * @param defaultValueSupplier 默认值供应商
     * @param <T>                  值类型
     * @return ThreadLocal 值或默认值
     */
    public <T> T getOrDefault(ThreadLocal<T> threadLocal, Supplier<T> defaultValueSupplier) {
        return ObjUtil.defaultIfNull(threadLocal.get(), defaultValueSupplier);
    }

}
