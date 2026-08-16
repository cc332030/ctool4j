package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

/**
 * <p>
 * Description: CTriFunction
 * </p>
 * <p>
 * 注意：apply 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/10/24
 */
@FunctionalInterface
public interface CTriFunction<O1, O2, O3, R> {

    /**
     * 应用函数处理三个参数（受检异常由内部包装处理）
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @param o3 第三个参数
     * @return 处理结果
     */
    @SneakyThrows
    default R apply(O1 o1, O2 o2, O3 o3) {
        return applyThrowable(o1, o2, o3);
    }

    /**
     * 应用函数处理三个参数，可抛出受检异常
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @param o3 第三个参数
     * @return 处理结果
     * @throws Throwable 处理过程中可能抛出的异常
     */
    R applyThrowable(O1 o1, O2 o2, O3 o3) throws Throwable;

    /**
     * 应用函数处理三个参数（function 为空时返回 null）
     * @param function 函数
     * @param o1 第一个参数
     * @param o2 第二个参数
     * @param o3 第三个参数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <O3> 第三个参数类型
     * @param <R> 结果类型
     * @return 处理结果
     */
    static <O1, O2, O3, R> R apply(CTriFunction<O1, O2, O3, R> function, O1 o1, O2 o2, O3 o3) {
        if(null == function) {
            return null;
        }
        return function.apply(o1, o2, o3);
    }

    /**
     * 转换为 CTriFunction 形式（function 为空时返回 null）
     * @param function 函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <O3> 第三个参数类型
     * @param <R> 结果类型
     * @return CTriFunction
     */
    static <O1, O2, O3, R> CTriFunction<O1, O2, O3, R> convert(CTriFunction<O1, O2, O3, R> function) {
        return (o1, o2, o3) -> apply(function, o1, o2, o3);
    }

    /**
     * 取第一个参数函数常量
     */
    CTriFunction<Object, Object, Object, Object> FIRST = (o1, o2, o3) -> o1;

    /**
     * 取第一个参数函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <O3> 第三个参数类型
     * @return 取第一个参数函数
     */
    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O1> first() {
        return (CTriFunction<O1, O2, O3, O1>)FIRST;
    }

    /**
     * 取第二个参数函数常量
     */
    CTriFunction<Object, Object, Object, Object> SECOND = (o1, o2, o3) -> o2;

    /**
     * 取第二个参数函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <O3> 第三个参数类型
     * @return 取第二个参数函数
     */
    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O2> second() {
        return (CTriFunction<O1, O2, O3, O2>)SECOND;
    }

    /**
     * 取第三个参数函数常量
     */
    CTriFunction<Object, Object, Object, Object> THIRD = (o1, o2, o3) -> o3;

    /**
     * 取第三个参数函数
     * @param <O1> 第一个参数类型
     * @param <O2> 第二个参数类型
     * @param <O3> 第三个参数类型
     * @return 取第三个参数函数
     */
    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O3> third() {
        return (CTriFunction<O1, O2, O3, O3>)THIRD;
    }

}
