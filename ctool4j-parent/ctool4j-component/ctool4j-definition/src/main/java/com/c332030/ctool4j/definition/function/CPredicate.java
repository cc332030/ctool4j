package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Predicate;

/**
 * <p>
 * Description: CPredicate
 * </p>
 * <p>
 * 注意：test 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CPredicate<T> extends Predicate<T> {

    /**
     * 测试参数（受检异常由内部包装处理）
     * @param t 参数
     * @return 测试结果
     */
    @Override
    @SneakyThrows
    default boolean test(T t) {
        return testThrowable(t);
    }

    /**
     * 测试参数，可抛出受检异常
     * @param t 参数
     * @return 测试结果
     * @throws Throwable 处理过程中可能抛出的异常
     */
    boolean testThrowable(T t) throws Throwable;

    /**
     * 恒为 true 的断言常量
     */
    CPredicate<Object> TRUE = t -> true;

    /**
     * 恒为 true 的断言
     * @param <T> 参数类型
     * @return 恒为 true 的断言
     */
    @SuppressWarnings("unchecked")
    static <T> CPredicate<T> alwaysTrue() {
        return (CPredicate<T>)TRUE;
    }

    /**
     * 恒为 false 的断言常量
     */
    CPredicate<Object> FALSE = t -> false;

    /**
     * 恒为 false 的断言
     * @param <T> 参数类型
     * @return 恒为 false 的断言
     */
    @SuppressWarnings("unchecked")
    static <T> CPredicate<T> alwaysFalse() {
        return (CPredicate<T>)FALSE;
    }

    /**
     * 测试参数（predicate 为空时返回 false）
     * @param predicate 断言
     * @param t 参数
     * @param <T> 参数类型
     * @return 测试结果
     */
    static <T> boolean test(Predicate<T> predicate, T t) {
        if(null == predicate) {
            return false;
        }
        return predicate.test(t);
    }

    /**
     * 转换为 JDK Predicate
     * @param predicate 自定义断言
     * @param <T> 参数类型
     * @return JDK Predicate
     */
    static <T> Predicate<T> convert(CPredicate<T> predicate) {
        return t -> test(predicate, t);
    }


}
