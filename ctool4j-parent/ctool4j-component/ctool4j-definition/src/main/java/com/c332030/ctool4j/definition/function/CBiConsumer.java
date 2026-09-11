package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CBiConsumer
 * </p>
 * <p>
 * 注意：accept 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBiConsumer&lt;T, U&gt;} 为双参消费者接口，扩展 {@code BiConsumer}，支持受检异常：</p>
 * <ul>
 *   <li>{@code accept}：默认方法，@SneakyThrows 包装后调用 {@code acceptThrowable}</li>
 *   <li>{@code acceptThrowable}：抽象方法，可抛 Throwable</li>
 *   <li>工具：{@code EMPTY}/{@code empty()}、{@code accept(consumer, t, u)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>accept(consumer=null, t, u)</td>
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
 *   <li>{@code accept} 内部 @SneakyThrows 包装（设计取舍）。</li>
 * </ul>
 * <p><b>工具方法</b></p>
 * <ul>
 *   <li>{@code empty()}：空实现；{@code accept(consumer, t, u)}：consumer 为 null 时不做处理。</li>
 * </ul>
 *
 * @since 2025/2/21
 * @version 1.0
 */
@FunctionalInterface
public interface CBiConsumer<T, U> extends BiConsumer<T, U> {

    /**
     * accept 方法名常量
     */
    String ACCEPT = "accept";

    /**
     * 消费两个参数（受检异常由内部包装处理）
     * @param t 第一个参数
     * @param u 第二个参数
     */
    @Override
    @SneakyThrows
    default void accept(T t, U u) {
        acceptThrowable(t, u);
    }

    /**
     * 消费两个参数，可抛出受检异常
     * @param t 第一个参数
     * @param u 第二个参数
     * @throws Throwable 处理过程中可能抛出的异常
     */
    void acceptThrowable(T t, U u) throws Throwable;

    /**
     * 空实现常量
     */
    CBiConsumer<Object, Object> EMPTY = (t, u) -> {};

    /**
     * 空实现
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 空实现
     */
    @SuppressWarnings("unchecked")
    static <T, U> CBiConsumer<T, U> empty() {
        return (CBiConsumer<T, U>)EMPTY;
    }

    /**
     * 消费两个参数（consumer 为空时不做处理）
     * @param consumer 消费者
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     */
    static <T, U> void accept(BiConsumer<T, U> consumer, T t, U u) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t, u);
    }

}
