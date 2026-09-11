package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CConsumer
 * </p>
 * <p>
 * 注意：accept 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CConsumer&lt;T&gt;} 为消费者接口，扩展 {@code Consumer&lt;T&gt;}，支持受检异常：</p>
 * <ul>
 *   <li>{@code accept}：默认方法，@SneakyThrows 包装后调用 {@code acceptThrowable}</li>
 *   <li>{@code acceptThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code EMPTY}/{@code empty()}、{@code accept(consumer, t)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>accept(consumer=null, t)</td>
 *     <td>不做处理</td>
 *   </tr>
 *   <tr>
 *     <td>empty()</td>
 *     <td>空实现</td>
 *   </tr>
 * </table>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>用 @SneakyThrows 简化受检异常处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>受检异常包装</b></p>
 * <ul>
 *   <li>{@code accept} 内部 @SneakyThrows 包装，调用方无法从签名感知受检异常（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code empty()}：空实现；{@code accept(consumer, t)}：consumer 为 null 时不做处理。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
@FunctionalInterface
public interface CConsumer<T> extends Consumer<T> {

    /**
     * 消费参数（受检异常由内部包装处理）
     * @param t 参数
     */
    @Override
    @SneakyThrows
    default void accept(T t) {
        acceptThrowable(t);
    }

    /**
     * 消费参数，可抛出受检异常
     * @param t 参数
     * @throws Throwable 处理过程中可能抛出的异常
     */
    void acceptThrowable(T t) throws Throwable;

    /**
     * 空实现常量
     */
    CConsumer<Object> EMPTY = (t) -> {};

    /**
     * 空实现
     * @param <T> 参数类型
     * @return 空实现
     */
    @SuppressWarnings("unchecked")
    static <T> CConsumer<T> empty() {
        return (CConsumer<T>)EMPTY;
    }

    /**
     * 消费参数（consumer 为空时不做处理）
     * @param consumer 消费者
     * @param t 参数
     * @param <T> 参数类型
     */
    static <T> void accept(Consumer<T> consumer, T t) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t);
    }

}
