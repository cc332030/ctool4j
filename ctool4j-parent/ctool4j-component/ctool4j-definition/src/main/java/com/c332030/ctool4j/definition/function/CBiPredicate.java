package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiPredicate;

/**
 * <p>
 * Description: CBiPredicate
 * </p>
 * <p>
 * 注意：test 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CBiPredicate<T, U> extends BiPredicate<T, U> {

    /**
     * 测试两个参数（受检异常由内部包装处理）
     * @param t 第一个参数
     * @param u 第二个参数
     * @return 测试结果
     */
    @Override
    @SneakyThrows
    default boolean test(T t, U u) {
        return testThrowable(t, u);
    }

    /**
     * 测试两个参数，可抛出受检异常
     * @param t 第一个参数
     * @param u 第二个参数
     * @return 测试结果
     * @throws Throwable 处理过程中可能抛出的异常
     */
    boolean testThrowable(T t, U u) throws Throwable;

    /**
     * 恒为 true 的断言常量
     */
    CBiPredicate<Object, Object> TRUE = (t, u) -> true;

    /**
     * 恒为 true 的断言
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 恒为 true 的断言
     */
    @SuppressWarnings("unchecked")
    static <T, U> CBiPredicate<T, U> alwaysTrue() {
        return (CBiPredicate<T, U>)TRUE;
    }

    /**
     * 恒为 false 的断言常量
     */
    CBiPredicate<Object, Object> FALSE = (t, u) -> false;

    /**
     * 恒为 false 的断言
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 恒为 false 的断言
     */
    @SuppressWarnings("unchecked")
    static <T, U> CBiPredicate<T, U> alwaysFalse() {
        return (CBiPredicate<T, U>)FALSE;
    }

    /**
     * 测试两个参数（predicate 为空时返回 false）
     * @param predicate 断言
     * @param t 第一个参数
     * @param u 第二个参数
     * @return 测试结果
     */
    static <T, U> boolean test(BiPredicate<T, U> predicate, T t, U u) {
        if(null == predicate) {
            return false;
        }
        return predicate.test(t, u);
    }

    /**
     * 转换为 JDK BiPredicate
     * @param predicate 自定义断言
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return JDK BiPredicate
     */
    static <T, U> BiPredicate<T, U> convert(CBiPredicate<T, U> predicate) {
        return (t, u) -> test(predicate, t, u);
    }


}
