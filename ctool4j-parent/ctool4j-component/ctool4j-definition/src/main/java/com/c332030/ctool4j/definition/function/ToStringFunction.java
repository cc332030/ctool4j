package com.c332030.ctool4j.definition.function;

/**
 * <p>
 * Description: ToStringFunction
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ToStringFunction&lt;T&gt;} 为转字符串函数接口，扩展 {@code CFunction&lt;T, String&gt;}，返回类型固定为 String。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对象转字符串的函数式处理（如显示转换）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅为 CFunction&lt;T, String&gt; 的类型别名。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>继承 CFunction 的受检异常包装与工具方法，返回类型固定为 String。</li>
 * </ul>
 *
 * @since 2024/12/2
 * @version 1.0
 */
@FunctionalInterface
public interface ToStringFunction<T> extends CFunction<T, String> {

}
