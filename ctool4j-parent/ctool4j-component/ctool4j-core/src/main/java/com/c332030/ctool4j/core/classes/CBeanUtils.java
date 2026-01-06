package com.c332030.ctool4j.core.classes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.function.*;
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

    /**
     * map 属性复制到对象
     * @param fromMap 源 map
     * @param to 目标对象
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copy(Map<String, ?> fromMap, To to) {

        if(MapUtil.isEmpty(fromMap) || null == to) {
            return to;
        }

        val toFieldMap = CReflectUtils.getInstanceFieldMap(to.getClass());
        toFieldMap.forEach((CBiConsumer<String, Field>)(toFieldName, toField) -> {

            val fromFieldValue = fromMap.get(toFieldName);
            if(null == fromFieldValue
                    || CReflectUtils.isStatic(toField)
                    || CReflectUtils.isFinal(toField)
            ) {
                return;
            }

            CConvertUtils.convertOpt(fromFieldValue, toField.getType())
                    .ifPresent((CConsumer<Object>) toValue -> toField.set(to, toValue));

        });

        return to;
    }

    /**
     * 对象属性复制
     * @param from 源对象
     * @param to 目标对象
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copy(Object from, To to) {
        return copy(toMap(from), to);
    }

    /**
     * 对象属性复制
     * @param fromMap 属性 map
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, Class<To> toClass) {
        if(MapUtil.isEmpty(fromMap)) {
            return null;
        }
        return copy(fromMap, CReflectUtils.newInstance(toClass));
    }

    /**
     * 对象属性复制
     * @param from 源对象
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, Class<To> toClass) {
        return copy(toMap(from), toClass);
    }

    /**
     * 对象属性复制
     * @param fromMap 源 map
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, CSupplier<To> toSupplier) {
        if(MapUtil.isEmpty(fromMap)) {
            return null;
        }
        return copy(fromMap, toSupplier.get());
    }

    /**
     * 对象属性复制
     * @param from 源对象
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, CSupplier<To> toSupplier) {
        return copy(toMap(from), toSupplier);
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyListFromMap(Collection<Map<String, ?>> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toClass 目标对象类型
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, Class<To> toClass) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return copyList(fromCollection, () -> CReflectUtils.newInstance(toClass));
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toSupplier 目标对象获取方法
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return copyListFromMap(CCollUtils.convert(fromCollection, CBeanUtils::toMap), toSupplier);
    }

    /**
     * 对象转 map，使用 json 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapJsonName(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, JsonProperty.class, JsonProperty::value);
    }

    /**
     * 对象转 map，使用注解 key
     * @param object 源对象
     * @param annotationClass 注解类
     * @param annotationValueFunction 注解值获取方法
     * @param <T> 注解泛型
     * @return 值 map
     */
    public <T extends Annotation> Map<String, Object> toMap(
            Object object,
            Class<T> annotationClass,
            CFunction<T, String> annotationValueFunction
    ) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, field -> CReflectUtils.getFieldName(field, annotationClass, annotationValueFunction));
    }

    /**
     * 对象转 map
     * @param object 对象
     * @return 值 map
     */
    public Map<String, Object> toMap(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, Field::getName);
    }

    /**
     * 对象转 map
     * @param object 对象
     * @param getFieldNameFunction 获取字段名方法
     * @return 对象值 map
     */
    public Map<String, Object> toMap(Object object, ToStringFunction<Field> getFieldNameFunction) {

        Class<?> objClass;
        if(null == object
                || CClassUtils.isJdkClass(objClass = object.getClass())
        ) {
            return CMap.of();
        }

        val fieldMap = CReflectUtils.getInstanceFieldMap(objClass);
        return CCollUtils.toMap(
                fieldMap.values(),
                getFieldNameFunction,
                (CFunction<Field, Object>) e -> CReflectUtils.getValue(object, e)
        );
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     * @param fromArr 源对象数组
     * @param to 目标对象
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, To to) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return to;
        }

        for (int i = fromArr.length-1; i >= 0; i--) {
            val source = fromArr[i];
            CBeanUtils.copy(source, to);
        }

        return to;
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     * @param fromArr 源对象数组
     * @param toClass 目标对象类
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, Class<To> toClass) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return null;
        }

        val to = CReflectUtils.newInstance(toClass);
        return copyFromArr(fromArr, to);
    }

}
