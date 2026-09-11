package com.c332030.ctool4j.core.util;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
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
 * <h2>能力目录</h2>
 * <p>{@code CEnumUtils} 为枚举工具类，提供：</p>
 * <ul>
 *   <li>{@code getNameMap}：枚举名（{@code name()}）到枚举的 Map</li>
 *   <li>{@code getMap}：枚举值（实现 {@code ICValue}）或指定字段值到枚举的 Map</li>
 *   <li>{@code values}：获取枚举所有值（不可变列表）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>反查值不存在</td>
 *     <td>抛 IllegalArgumentException（"no enum with value: ..."）</td>
 *   </tr>
 *   <tr>
 *     <td>非枚举类</td>
 *     <td>getMap 抛 IllegalArgumentException（"not enum"）</td>
 *   </tr>
 *   <tr>
 *     <td>字段不存在</td>
 *     <td>反射抛 NoSuchFieldException</td>
 *   </tr>
 *   <tr>
 *     <td>字段值为 null 的枚举</td>
 *     <td>不建立该值反查项（getMap 构建时过滤）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>按枚举名、枚举值（{@code ICValue}）、自定义字段值反查枚举。</li>
 *   <li>需要枚举所有值的不可变列表。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>字段值为 null 的枚举不参与反查 Map（无法用 null 作 Map key 稳定反查）。</li>
 *   <li>反查基于 Map 精确匹配，大小写敏感。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>字段值 Map 缓存无失效机制：枚举字段值静态不可变，风险低。</li>
 *   <li>{@code valueOf} 对不存在值抛异常而非返回 null，保证调用方明确感知查找失败。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>枚举值列表缓存（ENUM_VALUES）</b></p>
 * <ul>
 *   <li>基于 {@code ClassValue} 按枚举类缓存所有枚举值的不可变列表（{@code Collections.unmodifiableList}），</li>
 *   <li>线程安全、按类弱关联，避免重复 {@code getEnumConstants}。</li>
 * </ul>
 * <p><b>字段值 Map 缓存（VALUE_ENUM_MAP_CLASS_MAP）</b></p>
 * <ul>
 *   <li>按枚举类缓存"字段名 → 值 Map"的并发容器；字段值 Map 首次构建后以 {@code ConcurrentHashMap} 存入，</li>
 *   <li>避免重复反射遍历。</li>
 *   <li>构建规则：{@code ICName.NAME} 特殊处理（用 {@code Enum.name()}）；否则按指定字段反射取值，字段值非 null</li>
 *   <li>才放入 Map（null 值枚举不建立反查项）。</li>
 *   <li>加锁采用 {@code synchronized (enumClass)} 双重检查，保证单例构建。</li>
 * </ul>
 * <p><b>反查语义</b></p>
 * <ul>
 *   <li>值不存在抛 {@code IllegalArgumentException}。</li>
 *   <li>{@code nameOf(Class, name)} / {@code valueOf(Class, ...)}：先取对应 Map 再反查，异常语义同上。</li>
 * </ul>
 * <p><b>前置校验</b></p>
 * <ul>
 *   <li>指定字段名不存在时反射抛 {@code NoSuchFieldException}（经 {@code @SneakyThrows} 上抛）。</li>
 * </ul>
 *
 * @since 2024/4/7
 * @version 1.0
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
     * <h2>getMap 函数引用重载泛型签名</h2>
     * <ul>
     *   <li>{@code getMap(Class&lt;E&gt;, Func1&lt;E, T&gt;)}：{@code E} 为枚举类型（函数入参），{@code T} 为字段值类型（函数返回与 Map key），</li>
     *   <li>与 {@code valueOf(Class, Func1, value)} 的 {@code Func1&lt;C, T&gt;} 语义一致。</li>
     *   <li>行为等价于 {@code getMap(Class, fieldName)}（通过 {@code LambdaUtil.getFieldName} 取字段名），供字段名不便于手写</li>
     *   <li>或需编译期校验字段存在时使用。</li>
     * </ul>
     * <ul>
     *   <li>{@code getMap(Class, String)} 校验 {@code enumClass.isEnum()}，非枚举抛 {@code IllegalArgumentException}。</li>
     * </ul>
     *
     * @param enumClass 枚举类
     * @param <T>       枚举值类型
     * @param <E>       枚举类型
     * @return 枚举值到枚举的 Map*/
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
    public static <T, E> Map<T, E> getMap(Class<E> enumClass, Func1<E, T> func) {
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
                        val getterHandle = CMethodHandleUtils.toGetterHandle(field).asType(CMethodHandleUtils.GETTER_HANDLE_TYPE);
                        for (val val : values) {
                            val fieldValue = getterHandle.invoke(val);
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
