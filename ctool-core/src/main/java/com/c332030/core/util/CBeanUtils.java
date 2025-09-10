package com.c332030.core.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.c332030.core.function.CBiConsumer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: BeanUtils
 * </p>
 *
 * @author c332030
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CBeanUtils {

    public static final CBiConsumer<?, ?> EMPTY_COPY_CONSUMER= (o, t) -> {};

    private static final Map<Class<?>, Map<Class<?>, CBiConsumer<?, ?>>> BEAN_COPY_CONSUMER_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <From, To> CBiConsumer<From, To> getCopyConsumer(Class<From> fromClass, Class<To> toClass) {

        var convertMap = BEAN_COPY_CONSUMER_MAP.get(fromClass);
        CBiConsumer<From, To> copyConsumer;
        if(null == convertMap) {
            synchronized (BEAN_COPY_CONSUMER_MAP) {
                convertMap = BEAN_COPY_CONSUMER_MAP.computeIfAbsent(fromClass,
                        k -> new ConcurrentHashMap<>());
            }
        }

        copyConsumer = (CBiConsumer<From, To>)convertMap.get(toClass);
        if(null == copyConsumer) {
            synchronized (convertMap) {
                copyConsumer = (CBiConsumer<From, To>)convertMap.computeIfAbsent(toClass,
                        k -> createCopyConsumer(fromClass, toClass));
            }
        }

        return copyConsumer;
    }

    @SuppressWarnings("unchecked")
    public static <From, To> CBiConsumer<From, To> createCopyConsumer(Class<From> fromClass, Class<To> toClass) {

        if(CClassUtils.isBasicClass(fromClass) || CClassUtils.isBasicClass(toClass)) {
            log.warn("Unsupported create copy consumer from {} to {}", fromClass, toClass);
            return (CBiConsumer<From, To>)EMPTY_COPY_CONSUMER;
        }

        log.debug("Create copy consumer from {} to {}", fromClass, toClass);

        val fromFieldMap = CClassUtils.getFields(fromClass);
        val toFieldMap = CClassUtils.getFields(toClass);

        val copyConsumers = fromFieldMap.entrySet().stream()
                .map(entry -> {

                    val fromField = entry.getValue();
                    val toField = toFieldMap.get(entry.getKey());

                    if(null == toField
                            || CClassUtils.isStatic(toField)
                            || CClassUtils.isFinal(toField)
                    ) {
                        return null;
                    }

                    if(CClassUtils.isStatic(fromField)) {
                        return null;
                    }

                    val fromFieldType = fromField.getType();
                    val toFieldType = toField.getType();

                    // 类型不匹配
                    if(!toFieldType.isAssignableFrom(fromFieldType)) {

                        // 避免死循环，使用懒加载
                        if(fromClass == fromFieldType && toClass == toFieldType) {
                            return (CBiConsumer<From, To>) (from, to) -> {

                                val fromFieldValue = fromField.get(from);
                                if(from == fromFieldValue) {
                                    log.warn("忽略循环引用，From 类中有实例自己");
                                    return;
                                }

                                val toFieldValue = CClassUtils.newInstance(toFieldType);
                                toField.set(to, toFieldValue);
                                copy(fromFieldValue, toFieldValue);

                            };
                        }

                        if(!CClassUtils.hasConverter(fromFieldType, toFieldType)) {
                            return null;
                        }

                        return (CBiConsumer<From, To>) (from, to) -> {
                            val fromFieldValue = fromField.get(from);
                            val toFieldValue = CClassUtils.convert(fromFieldValue, toFieldType);
                            toField.set(to, toFieldValue);
                        };
                    }

                    if(Collection.class.isAssignableFrom(fromFieldType)
                            || Map.class.isAssignableFrom(fromFieldType)
                            || fromFieldType.isArray()
                    ) {
                        return null;
                    }

                    return (CBiConsumer<From, To>) (from, to) -> {

                        val value = fromField.get(from);
                        if(null == value) {
                            return;
                        }
                        toField.set(to, value);
                    };
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return (from, to) -> {
            for(val consumer: copyConsumers) {
                consumer.accept(from, to);
            }
        };
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <From, To> To copy(From from, To to) {

        if(null == from || null == to) {
            return to;
        }

        val consumer = (CBiConsumer<From, To>)getCopyConsumer(from.getClass(), to.getClass());
        consumer.accept(from, to);
        return to;
    }

    public static <From, To> To copy(From from, Class<To> toClass) {
        return copy(from, CClassUtils.newInstance(toClass));
    }

    public static <From, To> To copy(From from, Supplier<To> toSupplier) {
        return copy(from, toSupplier.get());
    }

    public static <From, To> List<To> copy(Collection<From> fromCollection, Supplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    public static Map<String, Object> toMap(Object object) {
        return toMap(object, false);
    }

    public static Map<String, Object> toMap(Object object, boolean useJsonName) {

        val fieldMap = CClassUtils.getFields(object.getClass());
        return CCollUtils.toMap(
                fieldMap.values(),
                field -> {
                    if(useJsonName) {
                        val jsonProperties = field.getAnnotationsByType(JsonProperty.class);
                        if(ArrayUtils.isNotEmpty(jsonProperties)) {
                            return jsonProperties[0].value();
                        }
                    }
                    return field.getName();
                },
                (Function<Field, Object>) e -> CClassUtils.getValue(object, e)
        );
    }

}
