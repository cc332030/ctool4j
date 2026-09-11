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
 * <h2>能力目录</h2>
 * <p>{@code CTriFunction&lt;O1, O2, O3, R&gt;} 为三参函数接口，支持受检异常：</p>
 * <ul>
 *   <li>{@code apply}：默认方法，@SneakyThrows 包装后调用 {@code applyThrowable}</li>
 *   <li>{@code applyThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code apply(function, o1, o2, o3)}、{@code first()}/{@code second()}/{@code third()}</li>
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
 *   <li>{@code apply(function, o1, o2, o3)}：function 为 null 返回 null。</li>
 *   <li>{@code first()}/{@code second()}/{@code third()}：分别返回取第一/第二/第三参数的函数。</li>
 * </ul>
 *
 * @since 2025/10/24
 * @version 1.0
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
