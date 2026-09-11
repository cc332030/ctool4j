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
 * <h2>能力目录</h2>
 * <p>{@code CBiFunction&lt;O1, O2, R&gt;} 为双参函数接口，扩展 {@code BiFunction}，支持受检异常：</p>
 * <ul>
 *   <li>{@code apply}：默认方法，@SneakyThrows 包装后调用 {@code applyThrowable}</li>
 *   <li>{@code applyThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code apply(function, o1, o2)}、{@code first()}/{@code second()}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>apply(function=null, ...)</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用 @SneakyThrows 简化受检异常处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>受检异常包装</b></p>
 * <ul>
 *   <li>{@code apply} 内部 @SneakyThrows 包装（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code apply(function, o1, o2)}：function 为 null 返回 null。</li>
 *   <li>{@code first()}/{@code second()}：分别返回取第一/第二参数的函数。</li>
 * </ul>
 *
 * @since 2025/5/12
 * @version 1.0
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
    static <O1, O2> CBiFunction<O1, O2, O1> first() {
        return (CBiFunction<O1, O2, O1>)FIRST;
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
    static <O1, O2> CBiFunction<O1, O2, O2> second() {
        return (CBiFunction<O1, O2, O2>)SECOND;
    }

}
