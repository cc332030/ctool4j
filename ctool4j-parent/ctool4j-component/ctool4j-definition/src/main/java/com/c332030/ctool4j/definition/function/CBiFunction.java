package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBiFunction
 * </p>
 * <p>
 * 注意：apply 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/5/12
 * @see doc/design/core/CBiFunction.adoc
 * @see doc/design/core/CBiFunctionTests.adoc
 */
@FunctionalInterface
public interface CBiFunction<O1, O2, R> extends BiFunction<O1, O2, R> {

    /**
     * 应用函数处理两个参数（受检异常由内部包装处理）
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @return 处理结果
     */
    @Override
    @SneakyThrows
    default R apply(O1 o1, O2 o2) {
        return applyThrowable(o1, o2);
    }

    /**
     * 应用函数处理两个参数，可抛出受检异常
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @return 处理结果
     * @throws Throwable 处理过程中可能抛出的异常
     */
    R applyThrowable(O1 o1, O2 o2) throws Throwable;

    /**
     * 应用函数处理两个参数（function 为空时返回 null）
     * @param function 函数
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <R> 结果类型
     * @return 处理结果
     */
    static <O1, O2, R> R apply(BiFunction<O1, O2, R> function, O1 o1, O2 o2) {
        if(null == function) {
            return null;
        }
        return function.apply(o1, o2);
    }

    /**
     * 转换为 JDK BiFunction
     * @param function 自定义函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <U> 结果类型
     * @return JDK BiFunction
     */
    static <O1, O2, U> BiFunction<O1, O2, U> convert(CBiFunction<O1, O2, U> function) {
        return (o1, o2) -> apply(function, o1, o2);
    }

    /**
     * 取第一个参数函数常量
     */
    CBiFunction<Object, Object, Object> FIRST = (o1, o2) -> o1;

    /**
     * 取第一个参数函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @return 取第一个参数函数
     */
    @SuppressWarnings("unchecked")
    static <O1, O2> BiFunction<O1, O2, O1> first() {
        return (BiFunction<O1, O2, O1>)FIRST;
    }

    /**
     * 取第二个参数函数常量
     */
    CBiFunction<Object, Object, Object> SECOND = (o1, o2) -> o2;

    /**
     * 取第二个参数函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @return 取第二个参数函数
     */
    @SuppressWarnings("unchecked")
    static <O1, O2> BiFunction<O1, O2, O2> second() {
        return (BiFunction<O1, O2, O2>)SECOND;
    }

}
