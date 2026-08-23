package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Function;

/**
 * <p>
 * Description: CFunction
 * </p>
 * <p>
 * 注意：apply 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/1/15
 * @see "doc/design/core/CFunction.adoc"
 * @see "doc/design/core/CFunctionTests.adoc"
 */
@FunctionalInterface
public interface CFunction<O, R> extends Function<O, R> {

    /**
     * apply 方法名常量
     */
    String APPLY = "apply";

    /**
     * 应用函数处理参数（受检异常由内部包装处理）
     * @param o 参数
     * @return 处理结果
     */
    @Override
    @SneakyThrows
    default R apply(O o) {
        return applyThrowable(o);
    }

    /**
     * 应用函数处理参数，可抛出受检异常
     * @param o 参数
     * @return 处理结果
     * @throws Throwable 处理过程中可能抛出的异常
     */
    R applyThrowable(O o) throws Throwable;

    /**
     * 返回自身函数常量
     */
    CFunction<Object, Object> SELF = o -> o;

    /**
     * 返回自身函数
     * @param <O> 参数类型
     * @return 返回自身函数
     */
    @SuppressWarnings("unchecked")
    static <O> CFunction<O, O> self() {
        return (CFunction<O, O>)SELF;
    }

    /**
     * 恒返回 null 的函数常量
     */
    CFunction<Object, Object> EMPTY = o -> null;

    /**
     * 恒返回 null 的函数
     * @param <O> 参数类型
     * @param <R> 结果类型
     * @return 恒返回 null 的函数
     */
    @SuppressWarnings("unchecked")
    static <O, R> CFunction<O, R> empty() {
        return (CFunction<O, R>)EMPTY;
    }

    /**
     * 应用函数处理参数（function 为空时返回 null）
     * @param function 函数
     * @param o 参数
     * @param <O> 参数类型
     * @param <R> 结果类型
     * @return 处理结果
     */
    static <O, R> R apply(Function<O, R> function, O o) {
        if(null == function) {
            return null;
        }
        return function.apply(o);
    }

    /**
     * 转换为 JDK Function
     * @param function 自定义函数
     * @param <O> 参数类型
     * @param <R> 结果类型
     * @return JDK Function
     */
    static <O, R> Function<O, R> convert(CFunction<O, R> function) {
        return t -> apply(function, t);
    }

}
