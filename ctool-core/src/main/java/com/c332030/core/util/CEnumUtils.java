package com.c332030.core.util;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.c332030.core.interfaces.IValue;
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
 */
@UtilityClass
public class CEnumUtils {

    private static final String VALUE = "value";

    private static final ClassValue<List<?>> ENUM_VALUES = new ClassValue<List<?>>() {
        @Override
        protected List<?> computeValue(Class<?> type) {
            return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(type.getEnumConstants())));
        }
    };

    private static final ClassValue<Map<String, Map<?, ?>>> VALUE_ENUM_MAP_CLASS_MAP = new ClassValue<Map<String, Map<?, ?>>>() {
        @Override
        protected Map<String, Map<?, ?>> computeValue(@NonNull Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    public static <T extends Serializable, E extends IValue<T>> Map<T, E> getMap(Class<E> enumClass) {
        return getMap(enumClass, VALUE);
    }

    public static <T, E> Map<T, E> getMap(Class<E> enumClass, Func1<T, ?> func) {
        return getMap(enumClass, LambdaUtil.getFieldName(func));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T, E> Map<T, E> getMap(Class<E> enumClass, String fieldName) {

        Assert.isTrue(enumClass.isEnum(), "not enum");

        val fieldValueMap = VALUE_ENUM_MAP_CLASS_MAP.get(enumClass);
        var valueMap = fieldValueMap.get(fieldName);
        if(valueMap == null) {
            synchronized (enumClass) {

                valueMap = fieldValueMap.get(fieldName);
                if(valueMap == null) {

                    val values = (List<E>)ENUM_VALUES.get(enumClass);

                    val map = new LinkedHashMap<>(values.size());

                    val field = enumClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    for(val val : values) {
                        val fieldValue = field.get(val);
                        if(fieldValue != null) {
                            map.put(fieldValue, val);
                        }
                    }

                    valueMap = Collections.unmodifiableMap(map);
                    fieldValueMap.put(fieldName, valueMap);
                }
            }
        }

        return (Map<T, E>)valueMap;
    }

    public static <T extends Serializable, E> E valueOf(Map<T, E> map, T value) {
        return Optional.ofNullable(map.get(value))
                .orElseThrow(() -> new IllegalArgumentException("no enum with value: " + value));
    }

    public static <T extends Serializable, C extends IValue<T>> C valueOf(Class<C> cClass, T value) {
        return valueOf(getMap(cClass), value);
    }

    public static <T extends Serializable, C extends Enum<C>> C valueOf(Class<C> cClass, Func1<C, T> func, T value) {
        return valueOf(getMap(cClass, LambdaUtil.getFieldName(func)), value);
    }

    public static <T extends Serializable, C extends Enum<C>> C valueOf(Class<C> cClass, String fieldName, T value) {
        return valueOf(getMap(cClass, fieldName), value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> List<T> values(Class<T> enumClass) {
        return (List<T>) ENUM_VALUES.get(enumClass);
    }

}
