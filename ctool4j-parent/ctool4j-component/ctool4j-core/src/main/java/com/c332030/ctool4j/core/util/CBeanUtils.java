package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.function.CBiConsumer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
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

    private static final Map<Class<?>, Map<Class<?>, BiConsumer<?, ?>>> BEAN_COPY_CONSUMER_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <From, To> BiConsumer<From, To> getCopyConsumer(Class<From> fromClass, Class<To> toClass) {

        var convertMap = BEAN_COPY_CONSUMER_MAP.get(fromClass);
        BiConsumer<From, To> copyConsumer;
        if(null == convertMap) {
            synchronized (BEAN_COPY_CONSUMER_MAP) {
                convertMap = BEAN_COPY_CONSUMER_MAP.computeIfAbsent(fromClass,
                        k -> new ConcurrentHashMap<>());
            }
        }

        copyConsumer = (BiConsumer<From, To>)convertMap.get(toClass);
        if(null == copyConsumer) {
            synchronized (convertMap) {
                copyConsumer = (BiConsumer<From, To>)convertMap.computeIfAbsent(toClass,
                        k -> createCopyConsumer(fromClass, toClass));
            }
        }

        return copyConsumer;
    }

    public <From, To> BiConsumer<From, To> createCopyConsumer(Class<From> fromClass, Class<To> toClass) {

        if(CClassUtils.isBasicClass(fromClass) || CClassUtils.isBasicClass(toClass)) {
            log.warn("Unsupported create copy consumer from {} to {}", fromClass, toClass);
            return CBiConsumer.empty();
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
                            val toFieldValue = CObjUtils.convert(fromFieldValue, toFieldType);
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

    @SuppressWarnings("unchecked")
    public <From, To> To copy(From from, To to) {

        if(null == from || null == to) {
            return to;
        }

        val consumer = (BiConsumer<From, To>)getCopyConsumer(from.getClass(), to.getClass());
        consumer.accept(from, to);
        return to;
    }

    public <From, To> To copy(From from, Class<To> toClass) {
        return copy(from, CClassUtils.newInstance(toClass));
    }

    public <From, To> To copy(From from, Supplier<To> toSupplier) {
        return copy(from, toSupplier.get());
    }

    public <From, To> List<To> copy(Collection<From> fromCollection, Supplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    public Map<String, Object> toMap(Object object) {
        return toMap(object, false);
    }

    public Map<String, Object> toMap(Object object, boolean useJsonName) {

        val fieldMap = CClassUtils.getFields(object.getClass());
        return CCollUtils.toMap(
                fieldMap.values(),
                field -> {
                    if(useJsonName) {
                        val jsonProperties = field.getAnnotationsByType(JsonProperty.class);
                        if(ArrayUtil.isNotEmpty(jsonProperties)) {
                            return jsonProperties[0].value();
                        }
                    }
                    return field.getName();
                },
                (Function<Field, Object>) e -> CClassUtils.getValue(object, e)
        );
    }

}
