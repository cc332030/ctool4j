package com.c332030.ctool4j.definition.function;

/**
 * <p>
 * Description: StringFunction
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code StringFunction&lt;T&gt;} 为字符串入参函数接口，扩展 {@code CFunction&lt;String, T&gt;}，固定入参为 String。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>入参为字符串的函数式处理（如解析、格式转换）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅为 CFunction&lt;String, T&gt; 的类型别名，无额外行为。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>继承 CFunction 的受检异常包装与工具方法，固定入参类型为 String，便于类型约束。</li>
 * </ul>
 *
 * @since 2024/12/2
 * @version 1.0
 */
@FunctionalInterface
public interface StringFunction<T> extends CFunction<String, T> {

}
