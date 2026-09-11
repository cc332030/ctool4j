package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import com.c332030.ctool4j.definition.function.CSupplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * <p>
 * Description: COpt
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code COpt&lt;T&gt;} 为可空值容器类，提供：</p>
 * <ul>
 *   <li>构造：{@code empty} / {@code of}（null 抛 NPE）/ {@code ofNullable} / {@code ofEmptyAble}（空字符串/空集合/空迭代器/空 Map 视为空）/ {@code ofBlankAble}（空白字符串视为空）</li>
 *   <li>取值：{@code get}（空抛 NoSuchElementException）/ {@code orElse} / {@code orElseGet} / {@code orElseThrow}</li>
 *   <li>转换：{@code map} / {@code flatMap} / {@code filter}</li>
 *   <li>判断与消费：{@code isPresent} / {@code ifPresent}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>of(null)</td>
 *     <td>抛 NullPointerException</td>
 *   </tr>
 *   <tr>
 *     <td>ofEmptyAble 空字符串/空集合/空 Map/空迭代器</td>
 *     <td>返回 empty</td>
 *   </tr>
 *   <tr>
 *     <td>ofBlankAble 空白字符串</td>
 *     <td>返回 empty</td>
 *   </tr>
 *   <tr>
 *     <td>get() 空值</td>
 *     <td>抛 NoSuchElementException</td>
 *   </tr>
 *   <tr>
 *     <td>orElse/orElseGet 空值</td>
 *     <td>返回默认值/经 supplier</td>
 *   </tr>
 *   <tr>
 *     <td>map/flatMap 函数返回 null</td>
 *     <td>返回 empty</td>
 *   </tr>
 *   <tr>
 *     <td>filter 不满足</td>
 *     <td>返回 empty</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要链式处理可能为空的值，且区分"空字符串/空集合"语义的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code of} 不接收 null；需空值用 {@code ofNullable} / {@code ofEmptyAble}。</li>
 *   <li>{@code get} 空值抛异常，需要空值时用 {@code orElse} 系列。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>提供比 JDK Optional 更丰富的空判定构造（空字符串/集合/Map），贴近业务空值语义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>构造语义</b></p>
 * <ul>
 *   <li>{@code of}：null 抛 {@code NullPointerException}。</li>
 *   <li>{@code ofEmptyAble}：多态重载，分别对 {@code CharSequence}（StrUtil.isEmpty）、{@code Iterable}（IterUtil.isEmpty）、</li>
 *   <li>{@code Collection}（CollUtil.isEmpty）、{@code Map}（MapUtil.isEmpty）做空判断，为空返回 empty。</li>
 *   <li>{@code ofBlankAble}：空白字符串（StrUtil.isBlank）视为空。</li>
 * </ul>
 * <p><b>取值与转换</b></p>
 * <ul>
 *   <li>{@code get}：值为 null 抛 {@code NoSuchElementException}。</li>
 *   <li>{@code orElse} / {@code orElseGet}：空时返回默认值/经 supplier 获取（惰性，值存在时不调用 supplier）。</li>
 *   <li>{@code orElseThrow}：空时抛出 supplier 提供的异常（@SneakyThrows）。</li>
 *   <li>{@code map}：函数返回 null 时返回 empty；{@code flatMap}：函数返回 null 时返回 empty。</li>
 *   <li>{@code filter}：不满足 predicate 返回 empty。</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class COpt<T> {

    /**
     * 空 COpt
     */
    public static final COpt<?> EMPTY = new COpt<>(null);

    private final T value;

    /**
     * 返回空 COpt
     * <ul>
     *   <li>空容器 {@code EMPTY} 单例，{@code empty()} 经 {@code CObjUtils.anyType} 做类型适配。</li>
     * </ul>
     *
     * @return 空 COpt
     * @param <T> 泛型
     */
    public static <T> COpt<T> empty() {
        return CObjUtils.anyType(EMPTY);
    }

    /**
     * 创建 COpt
     * @param t 值
     * @return COpt
     * @param <T> 泛型
     */
    public static <T> COpt<T> of(T t) {
        return new COpt<>(Objects.requireNonNull(t));
    }

    /**
     * 创建可空 COpt
     * @param value 值
     * @return COpt
     * @param <T> 泛型
     */
    public static <T> COpt<T> ofNullable(T value) {
        if(null == value) {
            return empty();
        }
        return of(value);
    }

    /**
     * 创建可空字符串 COpt
     * @param value CharSequence
     * @return COpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> COpt<T> ofEmptyAble(T value) {
        if(StrUtil.isEmpty(value)) {
            return empty();
        }
        return of(value);
    }

    /**
     * 创建可空字符串 COpt
     * @param value CharSequence
     * @return COpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> COpt<T> ofBlankAble(T value) {
        if(StrUtil.isBlank(value)) {
            return empty();
        }
        return of(value);
    }

    /**
     * 创建可空迭代器 COpt
     * @param value Iterable
     * @return COpt
     * @param <E> 值泛型
     * @param <T> Iterable 泛型
     */
    public static <E, T extends Iterable<E>> COpt<T> ofEmptyAble(T value) {
        if(IterUtil.isEmpty(value)) {
            return empty();
        }
        return of(value);
    }

    /**
     * 创建可空集合 COpt
     * @param value Collection
     * @return COpt
     * @param <E> 值泛型
     * @param <T> Collection 泛型
     */
    public static <E, T extends Collection<E>> COpt<T> ofEmptyAble(T value) {
        if(CollUtil.isEmpty(value)) {
            return empty();
        }
        return of(value);
    }

    /**
     * 创建可空 COpt
     * @param t Map
     * @return COpt
     * @param <T> Map 泛型
     * @param <K> 键泛型
     * @param <V> 值泛型
     */
    public static <K, V, T extends Map<K, V>> COpt<T> ofEmptyAble(T t) {
        if(MapUtil.isEmpty(t)) {
            return empty();
        }
        return new COpt<>(t);
    }

    /**
     * 获取值
     * @return 值
     */
    public T get() {
        if(null == value) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * 获取值，如果为空则返回默认值
     * @param defaultValue 默认值
     * @return 值
     */
    public T orElse(T defaultValue) {
        if(isPresent()) {
            return value;
        }
        return defaultValue;
    }

    /**
     * 获取值，如果为空则返回默认值
     * @param supplier 获取默认值的函数
     * @return 值
     */
    public T orElseGet(CSupplier<T> supplier) {
        if(isPresent()) {
            return value;
        }
        return supplier.get();
    }

    /**
     * 获取值，如果为空则抛出异常
     * @param supplier 获取异常的函数
     * @return 值
     * @param <E> 异常类型
     */
    @SneakyThrows
    public <E extends Throwable> T orElseThrow(CSupplier<E> supplier) {
        if(isPresent()) {
            return value;
        }

        throw supplier.get();
    }

    /**
     * 过滤，如果返回 false 则返回空
     * @param predicate 断言
     * @return COpt
     */
    public COpt<T> filter(CPredicate<? super T> predicate) {
        if(!isPresent()) {
            return empty();
        }

        if(predicate.test(value)) {
            return this;
        }

        return empty();
    }

    /**
     * 映射，如果返回 null 则返回空
     * @param function 函数
     * @return COpt
     * @param <R> 映射值泛型
     */
    public <R> COpt<R> map(CFunction<T, R> function) {
        if(!isPresent()) {
            return empty();
        }
        return ofNullable(function.apply(value));
    }

    /**
     * 映射，如果返回 null 则返回空
     * @param function 函数
     * @return COpt
     * @param <R> 映射值泛型
     */
    public <R> COpt<R> flatMap(CFunction<T, COpt<R>> function) {

        if(!isPresent()) {
            return empty();
        }

        val opt = function.apply(value);
        if(null != opt) {
            return opt;
        }

        return empty();
    }

    /**
     * 判断是否为空
     * @return boolean
     */
    public boolean isPresent() {
        return Objects.nonNull(value);
    }

    /**
     * 如果不为空，则执行操作
     * @param consumer 操作
     */
    public void ifPresent(CConsumer<T> consumer) {
        if(isPresent()) {
            consumer.accept(value);
        }
    }

    /**
     * toString
     * @return String
     */
    @Override
    public String toString() {
        val str = isPresent()
                ? "[" + value + "]"
                : ".empty";
        return getClass() + str;
    }

}
