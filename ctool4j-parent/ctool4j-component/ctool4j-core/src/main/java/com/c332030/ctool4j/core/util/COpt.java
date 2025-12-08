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
 * @since 2025/12/6
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class COpt<T> {

    public static final COpt<?> EMPTY = new COpt<>(null);

    protected final T value;

    /**
     * 返回空 COpt
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
     * 创建空字符串 CStrOpt
     * @return CStrOpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> CStrOpt<T> emptyStr() {
        return CObjUtils.anyType(CStrOpt.EMPTY);
    }

    /**
     * 创建集合 CStrOpt
     * @param value CharSequence
     * @return CStrOpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> CStrOpt<T> of(T value) {
        if(StrUtil.isEmpty(value)) {
            throw new NoSuchElementException("CharSequence is empty");
        }
        return new CStrOpt<>(value);
    }

    /**
     * 创建可空字符串 CStrOpt
     * @param value CharSequence
     * @return CStrOpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> CStrOpt<T> ofEmptyAble(T value) {
        if(StrUtil.isEmpty(value)) {
            return emptyStr();
        }
        return new CStrOpt<>(value);
    }

    /**
     * 创建可空字符串 CStrOpt
     * @param value CharSequence
     * @return CStrOpt
     * @param <T> 值泛型
     */
    public static <T extends CharSequence> CStrOpt<T> ofBlankAble(T value) {
        if(StrUtil.isBlank(value)) {
            return emptyStr();
        }
        return new CStrOpt<>(value);
    }

    /**
     * 创建空迭代器 CIterOpt
     * @return CIterOpt
     * @param <T> 值泛型
     */
    public static <T> CIterOpt<T> emptyIter() {
        return CObjUtils.anyType(CIterOpt.EMPTY);
    }

    /**
     * 创建迭代器 CIterOpt
     * @param value Iterable
     * @return CIterOpt
     * @param <T> 值泛型
     */
    public static <T> CIterOpt<T> of(Iterable<T> value) {
        if(IterUtil.isEmpty(value)) {
            throw new NoSuchElementException("Iterator is empty");
        }
        return new CIterOpt<>(value);
    }

    /**
     * 创建可空迭代器 CIterOpt
     * @param value Iterable
     * @return CIterOpt
     * @param <T> 值泛型
     */
    public static <T> CIterOpt<T> ofEmptyAble(Iterable<T> value) {
        if(IterUtil.isEmpty(value)) {
            return emptyIter();
        }
        return new CIterOpt<>(value);
    }

    /**
     * 创建空集合 CCollOpt
     * @return CCollOpt
     * @param <T> 值泛型
     */
    public static <T> CCollOpt<T> emptyColl() {
        return CObjUtils.anyType(CCollOpt.EMPTY);
    }

    /**
     * 创建集合 CCollOpt
     * @param value Collection
     * @return CCollOpt
     * @param <T> 值泛型
     */
    public static <T> CCollOpt<T> of(Collection<T> value) {
        if(CollUtil.isEmpty(value)) {
            throw new NoSuchElementException("Collection is empty");
        }
        return new CCollOpt<>(value);
    }

    /**
     * 创建可空集合 CCollOpt
     * @param value Collection
     * @return CCollOpt
     * @param <T> 值泛型
     */
    public static <T> CCollOpt<T> ofEmptyAble(Collection<T> value) {
        if(CollUtil.isEmpty(value)) {
            return emptyColl();
        }
        return new CCollOpt<>(value);
    }

    /**
     * 创建空 Map CMapOpt
     * @return CMapOpt
     * @param <K> 键泛型
     * @param <V> 值泛型
     */
    public static <K, V> CMapOpt<K, V> emptyMap() {
        return CObjUtils.anyType(CMapOpt.EMPTY);
    }

    /**
     * 创建 Map CMapOpt
     * @param t Map
     * @return CMapOpt
     * @param <K> 键泛型
     * @param <V> 值泛型
     */
    public static <K, V> CMapOpt<K, V> of(Map<K, V> t) {
        if(MapUtil.isEmpty(t)) {
            throw new NoSuchElementException("Map is empty");
        }
        return new CMapOpt<>(t);
    }

    /**
     * 创建可空 CMapOpt
     * @param t Map
     * @return CMapOpt
     * @param <K> 键泛型
     * @param <V> 值泛型
     */
    public static <K, V> CMapOpt<K, V> ofEmptyAble(Map<K, V> t) {
        if(MapUtil.isEmpty(t)) {
            return emptyMap();
        }
        return new CMapOpt<>(t);
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
        return orElse(supplier.get());
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
