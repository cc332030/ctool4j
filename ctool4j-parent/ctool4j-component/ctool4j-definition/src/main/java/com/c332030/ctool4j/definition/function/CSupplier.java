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
 * <h2>能力目录</h2>
 * <p>{@code CSupplier&lt;T&gt;} 为供应器接口，扩展 {@code Supplier&lt;T&gt;}，支持受检异常：</p>
 * <ul>
 *   <li>{@code get}：默认方法，@SneakyThrows 包装后调用 {@code getThrowable}</li>
 *   <li>{@code getThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code NULL}/{@code alwaysNull()}、{@code get(supplier)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>get(supplier=null)</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>alwaysNull()</td>
 *     <td>恒返回 null</td>
 *   </tr>
 * </table>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用 @SneakyThrows 简化受检异常处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>受检异常包装</b></p>
 * <ul>
 *   <li>{@code get} 内部 @SneakyThrows 包装（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code alwaysNull()}：恒返回 null；{@code get(supplier)}：supplier 为 null 返回 null。</li>
 * </ul>
 *
 * @since 2025/1/15
 * @version 1.0
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
