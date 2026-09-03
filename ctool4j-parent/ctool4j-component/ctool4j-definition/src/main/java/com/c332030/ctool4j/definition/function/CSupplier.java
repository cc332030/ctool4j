package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CSupplier
 * </p>
 * <p>
 * 注意：get 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/1/15
 * @see "doc/design/core/CSupplier.adoc"
 * @see "doc/design/core/CSupplierTests.adoc"
 */
@FunctionalInterface
public interface CSupplier<T> extends Supplier<T> {

    /**
     * 获取结果（受检异常由内部包装处理）
     * @return 结果
     */
    @Override
    @SneakyThrows
    default T get() {
        return getThrowable();
    }

    /**
     * 获取结果，可抛出受检异常
     * @return 结果
     * @throws Throwable 获取过程中可能抛出的异常
     */
    T getThrowable() throws Throwable;

    /**
     * 恒返回 null 的供应器常量
     */
    CSupplier<Object> NULL = () -> null;

    /**
     * 恒返回 null 的供应器
     * @param <T> 结果类型
     * @return 恒返回 null 的供应器
     */
    @SuppressWarnings("unchecked")
    static <T> CSupplier<T> alwaysNull() {
        return (CSupplier<T>)NULL;
    }

    /**
     * 获取结果（supplier 为空时返回 null）
     * @param supplier 供应器
     * @param <T> 结果类型
     * @return 结果
     */
    static <T> T get(Supplier<T> supplier) {
        if(supplier == null) {
            return null;
        }
        return supplier.get();
    }

}
