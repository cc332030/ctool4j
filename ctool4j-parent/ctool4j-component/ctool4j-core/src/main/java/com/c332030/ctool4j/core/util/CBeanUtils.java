package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.core.function.CBiConsumer;
import com.c332030.ctool4j.core.function.CConsumer;
import com.c332030.ctool4j.core.function.CFunction;
import com.c332030.ctool4j.core.function.ToStringFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public <To> To copy(Map<String, ?> fromMap, To to) {

        if(MapUtil.isEmpty(fromMap) || null == to) {
            return to;
        }

        val toFieldMap = CClassUtils.getFields(to.getClass());
        toFieldMap.forEach((CBiConsumer<String, Field>)(toFieldName, toField) -> {

            val fromFieldValue = fromMap.get(toFieldName);
            if(null == fromFieldValue
                    || CClassUtils.isStatic(toField)
                    || CClassUtils.isFinal(toField)
            ) {
                return;
            }

            CClassUtils.convertOpt(fromFieldValue, toField.getType())
                    .ifPresent((CConsumer<Object>) toValue -> toField.set(to, toValue));

        });

        return to;
    }

    public <To> To copy(Object from, To to) {
        return copy(toMap(from), to);
    }

    public <To> To copy(Map<String, ?> fromMap, Class<To> toClass) {
        return copy(fromMap, CClassUtils.newInstance(toClass));
    }

    public <To> To copy(Object from, Class<To> toClass) {
        return copy(toMap(from), toClass);
    }

    public <To> To copy(Map<String, ?> fromMap, Supplier<To> toSupplier) {
        return copy(fromMap, toSupplier.get());
    }
    public <To> To copy(Object from, Supplier<To> toSupplier) {
        return copy(toMap(from), toSupplier);
    }

    public <To> List<To> copyListFromMap(Collection<Map<String, ?>> fromCollection, Supplier<To> toSupplier) {

        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    public <To> List<To> copyList(Collection<?> fromCollection, Supplier<To> toSupplier) {
        return copyListFromMap(CCollUtils.convert(fromCollection, CBeanUtils::toMap), toSupplier);
    }

    /**
     * 对象转 map
     * @param object 源对象
     * @param useJsonName 是否使用 json 配置的属性名
     * @return 值 map
     */
    @Deprecated
    public Map<String, Object> toMap(Object object, boolean useJsonName) {

        if(useJsonName) {
            return toMapJsonName(object);
        }
        return toMap(object);
    }

    /**
     * 对象转 map，使用 json 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapJsonName(Object object) {
        return toMap(object, JsonProperty.class, JsonProperty::value);
    }

    /**
     * 对象转 map
     * @param object 源对象
     * @param annotationClass 注解类
     * @param annotationValueFunction 注解值获取方法
     * @return 值 map
     */
    public <T extends Annotation> Map<String, Object> toMap(
            Object object,
            Class<T> annotationClass,
            CFunction<T, String> annotationValueFunction
    ) {
        return toMap(object, field -> CClassUtils.getFieldName(field, annotationClass, annotationValueFunction));
    }

    /**
     * 对象转 map
     * @param object 对象
     * @return 值 map
     */
    public Map<String, Object> toMap(Object object) {
        return toMap(object, Field::getName);
    }

    /**
     * 对象转 map
     * @param object 对象
     * @param getFieldNameFunction 获取字段名方法
     * @return 对象值 map
     */
    public Map<String, Object> toMap(Object object, ToStringFunction<Field> getFieldNameFunction) {

        if(null == object) {
            return CMap.of();
        }

        val fieldMap = CClassUtils.getFields(object.getClass());
        return CCollUtils.toMap(
                fieldMap.values(),
                getFieldNameFunction,
                (Function<Field, Object>) e -> CClassUtils.getValue(object, e)
        );
    }

}
