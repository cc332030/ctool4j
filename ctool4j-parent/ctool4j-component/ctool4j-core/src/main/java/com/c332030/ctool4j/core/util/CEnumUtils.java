package com.c332030.ctool4j.core.util;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.c332030.ctool4j.definition.interfaces.ICName;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CEnumUtils
 * </p>
 *
 * @since 2024/4/7
 * @see doc/design/core/CEnumUtils.adoc
 * @see doc/design/core/CEnumUtilsTests.adoc
 */
@UtilityClass
public class CEnumUtils {

    private static final ClassValue<List<?>> ENUM_VALUES = new ClassValue<List<?>>() {
        /**
         * 缓存枚举所有值的不可变列表
         *
         * @param type 枚举类
         * @return 枚举值列表
         */
        @Override
        protected List<?> computeValue(Class<?> type) {
            return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(type.getEnumConstants())));
        }
    };

    private static final ClassValue<Map<String, Map<?, ?>>> VALUE_ENUM_MAP_CLASS_MAP = new ClassValue<Map<String, Map<?, ?>>>() {
        /**
         * 缓存枚举字段名到值 Map 的容器（各枚举独立）
         *
         * @param type 枚举类
         * @return 字段名到值 Map 的并发容器
         */
        @Override
        protected Map<String, Map<?, ?>> computeValue(@NonNull Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    /**
     * 获取枚举名到枚举的 Map
     *
     * @param enumClass 枚举类
     * @param <E>       枚举类型
     * @return 枚举名到枚举的 Map
     */
    public static <E> Map<String, E> getNameMap(Class<E> enumClass) {
        return getMap(enumClass, ICName.NAME);
    }

    /**
     * 获取枚举值到枚举的 Map
     *
     * @param enumClass 枚举类
     * @param <T>       枚举值类型
     * @param <E>       枚举类型
     * @return 枚举值到枚举的 Map
     */
    public static <T extends Serializable, E extends ICValue<T>> Map<T, E> getMap(Class<E> enumClass) {
        return getMap(enumClass, ICValue.VALUE);
    }

    /**
     * 获取指定字段值到枚举的 Map
     *
     * @param enumClass 枚举类
     * @param func      字段引用
     * @param <T>       字段值类型
     * @param <E>       枚举类型
     * @return 字段值到枚举的 Map
     */
    public static <T, E> Map<T, E> getMap(Class<E> enumClass, Func1<T, ?> func) {
        return getMap(enumClass, LambdaUtil.getFieldName(func));
    }

    /**
     * 获取指定字段值到枚举的 Map
     *
     * @param enumClass 枚举类
     * @param fieldName 字段名
     * @param <T>       字段值类型
     * @param <E>       枚举类型
     * @return 字段值到枚举的 Map
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T, E> Map<T, E> getMap(Class<E> enumClass, String fieldName) {

        Assert.isTrue(enumClass.isEnum(), "not enum");

        val fieldValueMap = VALUE_ENUM_MAP_CLASS_MAP.get(enumClass);
        var valueMap = fieldValueMap.get(fieldName);
        if (valueMap == null) {
            synchronized (enumClass) {

                valueMap = fieldValueMap.get(fieldName);
                if (valueMap == null) {

                    val values = (List<E>) ENUM_VALUES.get(enumClass);

                    val map = new LinkedHashMap<>(values.size());
                    if (ICName.NAME.equals(fieldName)) {
                        values.forEach(value -> map.put(((Enum<?>) value).name(), value));
                    } else {

                        val field = enumClass.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        for (val val : values) {
                            val fieldValue = field.get(val);
                            if (fieldValue != null) {
                                map.put(fieldValue, val);
                            }
                        }
                    }
                    valueMap = Collections.unmodifiableMap(map);
                    fieldValueMap.put(fieldName, valueMap);
                }
            }
        }

        return (Map<T, E>) valueMap;
    }

    /**
     * 根据值从 Map 中获取枚举
     *
     * @param map  枚举 Map
     * @param value 值
     * @param <T>  值类型
     * @param <E>  枚举类型
     * @return 枚举
     * @throws IllegalArgumentException 值不存在时抛出
     */
    public static <T extends Serializable, E> E valueOf(Map<T, E> map, T value) {
        return Optional.ofNullable(map.get(value))
                .orElseThrow(() -> new IllegalArgumentException("no enum with value: " + value));
    }

    /**
     * 根据枚举名获取枚举
     *
     * @param cClass 枚举类
     * @param value  枚举名
     * @param <E>    枚举类型
     * @return 枚举
     * @throws IllegalArgumentException 枚举名不存在时抛出
     */
    public static <E> E nameOf(Class<E> cClass, String value) {
        return valueOf(getNameMap(cClass), value);
    }

    /**
     * 根据值获取枚举（枚举值实现 ICValue）
     *
     * @param cClass 枚举类
     * @param value  枚举值
     * @param <T>    值类型
     * @param <C>    枚举类型
     * @return 枚举
     * @throws IllegalArgumentException 值不存在时抛出
     */
    public static <T extends Serializable, C extends ICValue<T>> C valueOf(Class<C> cClass, T value) {
        return valueOf(getMap(cClass, ICValue.VALUE), value);
    }

    /**
     * 根据字段引用的值获取枚举
     *
     * @param cClass 枚举类
     * @param func   字段引用
     * @param value  字段值
     * @param <T>    值类型
     * @param <C>    枚举类型
     * @return 枚举
     * @throws IllegalArgumentException 值不存在时抛出
     */
    public static <T extends Serializable, C extends Enum<C>> C valueOf(Class<C> cClass, Func1<C, T> func, T value) {
        return valueOf(getMap(cClass, LambdaUtil.getFieldName(func)), value);
    }

    /**
     * 根据指定字段名的值获取枚举
     *
     * @param cClass    枚举类
     * @param fieldName 字段名
     * @param value     字段值
     * @param <T>       值类型
     * @param <C>       枚举类型
     * @return 枚举
     * @throws IllegalArgumentException 值不存在时抛出
     */
    public static <T extends Serializable, C extends Enum<C>> C valueOf(Class<C> cClass, String fieldName, T value) {
        return valueOf(getMap(cClass, fieldName), value);
    }

    /**
     * 获取枚举所有值
     *
     * @param enumClass 枚举类
     * @param <T>       枚举类型
     * @return 枚举值 List
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> List<T> values(Class<T> enumClass) {
        return (List<T>) ENUM_VALUES.get(enumClass);
    }

}
