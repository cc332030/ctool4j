package com.c332030.ctool4j.core.classes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CReflectUtils
 * </p>
 *
 * @since 2024/4/2
 */
@CustomLog
@UtilityClass
public class CReflectUtils {

    public static final CClassValue<Map<String, Field>> FIELD_MAP_CLASS_VALUE =
            CClassValue.of(type -> CClassUtils.get(
                    type,
                    Class::getDeclaredFields,
                    Field::getName,
                    field -> {
                        field.setAccessible(true);
                        return field;
                    }
            ));

    private static final CClassValue<Map<Integer, List<Constructor<?>>>> CONSTRUCTOR_MAP_CLASS_VALUE =
            CClassValue.of(type -> {

                val constructors = type.getConstructors();
                for (val constructor : constructors) {
                    constructor.setAccessible(true);
                }
                return Arrays.stream(constructors)
                        .collect(Collectors.groupingBy(Constructor::getParameterCount));
            });

    public List<Constructor<?>> getConstructors(Class<?> tClass, Object... args) {

        val argTypes = CArrUtils.convert(args, Object::getClass);
        return getConstructors(tClass, argTypes);
    }

    public List<Constructor<?>> getConstructors(Class<?> tClass, Class<?>... argTypes) {

        val argsLength = ArrayUtil.length(argTypes);

        val constructorMap = CONSTRUCTOR_MAP_CLASS_VALUE.get(tClass);
        val constructors = constructorMap.get(argsLength);
        if(argsLength == 0) {
            return constructors;
        }

        val matchConstructors = new ArrayList<Constructor<?>>();
        for (val constructor : constructors) {

            var match = true;
            val parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {

                val argType = argTypes[i];
                val parameterType = parameterTypes[i];

                if(null != argType
                        && !parameterType.isAssignableFrom(argType)
                ) {
                    match = false;
                    break;
                }
            }

            if(match) {
                matchConstructors.add(constructor);
            }
        }

        return matchConstructors;
    }

    public Constructor<?> getNoArgConstructor(Class<?> tClass) {

        val constructors = getConstructors(tClass);
        return CCollUtils.first(constructors);
    }

    @SneakyThrows
    public <T> T newInstance(Class<T> tClass) {

        val noArgConstructor = getNoArgConstructor(tClass);
        CAssert.notNull(noArgConstructor, () -> " can't find no arg constructor, class: " + tClass);

        return CObjUtils.anyType(noArgConstructor.newInstance());
    }

    public static final CClassValue<List<Method>> METHODS_CLASS_VALUE = CClassValue.of(
            type -> CClassUtils.get(type, Class::getDeclaredMethods));

    public List<Method> getMethods(Class<?> type) {
        return METHODS_CLASS_VALUE.get(type);
    }

    public static final CClassValue<Map<String, List<Method>>> METHOD_MAP_CLASS_VALUE =
            CClassValue.of(type -> METHODS_CLASS_VALUE.get(type)
                    .stream()
                    .collect(Collectors.groupingBy(Method::getName))
            );

    public Map<String, List<Method>> getMethodsMap(Class<?> type) {
        return METHOD_MAP_CLASS_VALUE.get(type);
    }

    public List<Method> getMethodsByName(Class<?> type, String methodName) {
        return getMethodsMap(type).get(methodName);
    }

    public <T extends Annotation> String getFieldName(
            Field field,
            Class<T> annotationClass,
            CFunction<T, String> annotationValueFunction
    ) {

        val annotation = field.getAnnotation(annotationClass);
        if(null != annotation) {
            return annotationValueFunction.apply(annotation);
        }

        return field.getName();
    }

    public Map<String, Field> getFieldAllMap(Class<?> type) {
        return FIELD_MAP_CLASS_VALUE.get(type);
    }

    public Map<String, Field> getFieldMap(Class<?> type) {
        return FIELD_MAP_CLASS_VALUE.get(type).entrySet()
            .stream()
            .filter(entry -> !isStatic(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Field getField(Class<?> type, String fieldName) {
        return Optional.ofNullable(getFieldMap(type).get(fieldName))
                .orElseThrow(() -> new IllegalArgumentException(type + " no field with name: " + fieldName));
    }

    public boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    public boolean isStatic(Method field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isFinal(Method field) {
        return Modifier.isFinal(field.getModifiers());
    }

    public <T> T getValue(Object object, String fieldName) {
        return getValue(object, getFieldMap(object.getClass()).get(fieldName));
    }

    public <T> T getValue(Object object, Field field) {
        return getValue(object, field, false);
    }

    @SneakyThrows
    public <T> T getValue(Object object, Field field, boolean accessible) {
        if (!accessible) {
            field.setAccessible(true);
        }
        return CObjUtils.anyType(field.get(object));
    }

    public void setValue(Object object, String fieldName, Object value) {
        setValue(object, getFieldMap(object.getClass()).get(fieldName), value, true);
    }


    public void setValue(Object object, Field field, Object value) {
        setValue(object, field, value, false);
    }

    @SneakyThrows
    public void setValue(Object object, Field field, Object value, boolean accessible) {
        if (!accessible) {
            field.setAccessible(true);
        }
        field.set(object, value);
    }

    public <T> T fillValues(Class<T> clazz, Map<String, Object> fields) {

        if (MapUtil.isEmpty(fields)) {
            return null;
        }

        val object = newInstance(clazz);
        fillValues(object, fields);
        return object;
    }

    @SneakyThrows
    public void fillValues(Object object, Map<String, Object> fieldValueMap) {

        if (MapUtil.isEmpty(fieldValueMap)) {
            return;
        }

        val fieldMap = getFieldMap(object.getClass());

        for (val entry : fieldValueMap.entrySet()) {

            val fieldName = entry.getKey();
            val value = entry.getValue();

            val field = fieldMap.get(fieldName);
            if (null == field) {
                continue;
            }
            field.set(object, value);
        }

    }

    public String toSetMethodName(String getMethodName) {
        return "s" + getMethodName.substring(1);
    }

    public String toGetMethodName(String setMethodName) {
        return "g" + setMethodName.substring(1);
    }

    public <T> T invokeIgnoreNoMethod(Object value, String methodName, Object... args) {
        return invoke(value, methodName, true, args);
    }

    public <T> T invokeMustHaveMethod(Object value, String methodName, Object... args) {
        return invoke(value, methodName, false, args);
    }

    @SneakyThrows
    public <T> T invoke(Object value, String methodName, boolean ignoreNoMethod, Object... args) {

        Class<?> clazz = value.getClass();

        val methods = getMethodsByName(clazz, methodName);
        val method = CCollUtils.onlyOne(methods);
        if (null == method) {
            if (ignoreNoMethod) {
                return null;
            }
            throw new IllegalStateException("can't find method: " + methodName + " in class: " + clazz);
        }

        return CObjUtils.anyType(method.invoke(value, args));
    }

    /**
     * 获取注解值
     * @param executable Class/Method/Field
     * @param annotationClass 注解类
     * @param valueFunction 获取注解值的方法
     * @return 注解值
     * @param <A> 注解类泛型
     * @param <T> 返回值泛型
     */
    public <A extends Annotation, T> T getAnnotationValue(
            Executable executable,
            Class<A> annotationClass,
            Function<A, T> valueFunction
    ) {

        val annotation = executable.getAnnotation(annotationClass);
        if(null != annotation) {
            return valueFunction.apply(annotation);
        }

        return null;
    }

}
