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
 * <h2>能力目录</h2>
 * <p>{@code CPredicate&lt;T&gt;} 为断言接口，扩展 {@code Predicate&lt;T&gt;}，支持受检异常：</p>
 * <ul>
 *   <li>{@code test}：默认方法，@SneakyThrows 包装后调用 {@code testThrowable}</li>
 *   <li>{@code testThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code alwaysTrue}/{@code alwaysFalse}、{@code test(predicate, t)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>test(predicate=null, t)</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>alwaysTrue()</td>
 *     <td>恒 true</td>
 *   </tr>
 *   <tr>
 *     <td>alwaysFalse()</td>
 *     <td>恒 false</td>
 *   </tr>
 * </table>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用 @SneakyThrows 简化受检异常处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>受检异常包装</b></p>
 * <ul>
 *   <li>{@code test} 内部 @SneakyThrows 包装（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code alwaysTrue()}/{@code alwaysFalse()}：恒 true/false；{@code test(predicate, t)}：predicate 为 null 返回 false。</li>
 * </ul>
 *
 * @since 2025/1/15
 * @version 1.0
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

}
