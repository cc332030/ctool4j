package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.function.CBiConsumer;
import com.c332030.ctool4j.core.function.CConsumer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

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

    public Map<String, Object> toMap(Object object) {
        return toMap(object, false);
    }

    public Map<String, Object> toMap(Object object, boolean useJsonName) {

        if(null == object) {
            return CMap.of();
        }

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
