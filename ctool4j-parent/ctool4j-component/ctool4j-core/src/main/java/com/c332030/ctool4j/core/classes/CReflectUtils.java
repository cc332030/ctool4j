package com.c332030.ctool4j.core.classes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
            CClassValue.of(type -> CClassUtils.getMap(
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

    public Map<Integer, List<Constructor<?>>> getAllConstructors(Class<?> tClass) {
        return CONSTRUCTOR_MAP_CLASS_VALUE.get(tClass);
    }

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

    @SneakyThrows
    public <T> T newInstance(Constructor<T> constructor, Object... args) {
        return constructor.newInstance(args);
    }

    /**
     * 获取当前类的方法
     * @param type 类
     * @return 方法列表
     */
    public List<Method> getMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
            .peek(method -> method.setAccessible(true))
            .collect(Collectors.toList());
    }

    public List<Method> getAllMethods(Class<?> type) {
        return CClassUtils.getMap(type, Class::getDeclaredMethods);
    }

    public static final CClassValue<List<Method>> ALL_METHODS_CLASS_VALUE =
        CClassValue.of(CReflectUtils::getAllMethods);

    public List<Method> getAllMethodsCached(Class<?> type) {
        return ALL_METHODS_CLASS_VALUE.get(type);
    }

    public static final CClassValue<Map<String, List<Method>>> METHOD_MAP_CLASS_VALUE =
            CClassValue.of(type -> ALL_METHODS_CLASS_VALUE.get(type)
                    .stream()
                    .collect(Collectors.groupingBy(Method::getName))
            );

    public Map<String, List<Method>> getAllMethodsMap(Class<?> type) {
        return METHOD_MAP_CLASS_VALUE.get(type);
    }

    public List<Method> getAllMethodsByName(Class<?> type, String methodName) {
        return getAllMethodsMap(type).get(methodName);
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

    public Map<String, Field> getAllFieldMap(Class<?> type) {
        return FIELD_MAP_CLASS_VALUE.get(type);
    }

    public Map<String, Field> getFieldMap(Class<?> type, CPredicate<Field> predicate) {
        return CMapUtils.filterValue(
            getAllFieldMap(type),
            predicate
        );
    }

    /**
     * 获取类的静态变量 map
     * @param type 类
     * @return 静态变量 map
     */
    public Map<String, Field> getStaticFieldMap(Class<?> type) {
        return getFieldMap(type, CReflectUtils::isStatic);
    }

    /**
     * 获取类的实例变量 map
     * @param type 类
     * @return 实例变量 map
     */
    public Map<String, Field> getInstanceFieldMap(Class<?> type) {
        return getFieldMap(type, e -> !CReflectUtils.isStatic(e));
    }

    public Field getField(Class<?> type, String fieldName) {
        return Optional.ofNullable(getAllFieldMap(type).get(fieldName))
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
        return getValue(object, getAllFieldMap(object.getClass()).get(fieldName));
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
        setValue(object, getAllFieldMap(object.getClass()).get(fieldName), value, true);
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

    public <T> T fillValues(Class<T> clazz, Map<String, ?> fields) {

        if (MapUtil.isEmpty(fields)) {
            return null;
        }

        val object = newInstance(clazz);
        fillValues(object, fields);
        return object;
    }

    @SneakyThrows
    public void fillValues(Object object, Map<String, ?> fieldValueMap) {

        if (MapUtil.isEmpty(fieldValueMap)) {
            return;
        }

        val fieldMap = getInstanceFieldMap(object.getClass());
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

        val methods = getAllMethodsByName(clazz, methodName);
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
     * 方法/字段/类 注解缓存
     */
    final Map<Object, Map<Class<? extends Annotation>, Object>> ELEMENT_ANNOTATION_MAP = new ConcurrentHashMap<>();

    /**
     * 获取方法/字段/类 注解
     * @param element 元素
     * @param annotationClass 注解类
     * @return 注解
     * @param <T> 注解类泛型
     */
    public <ELEMENT, T extends Annotation> T getAnnotationCached(
        ELEMENT element,
        Function<Class<T>, Annotation> getAnnoFunction,
        Class<T> annotationClass
    ) {

        var annotationMap = ELEMENT_ANNOTATION_MAP.get(element);
        if(null == annotationMap) {
            synchronized (ELEMENT_ANNOTATION_MAP) {
                annotationMap = ELEMENT_ANNOTATION_MAP
                    .computeIfAbsent(element, k -> new ConcurrentHashMap<>());
            }
        }

        var annotation = annotationMap.get(annotationClass);
        if(null == annotation) {
            synchronized (annotationMap) {
                annotation = annotationMap
                    .computeIfAbsent(annotationClass, k -> {

                        val anno = getAnnoFunction.apply(annotationClass);
                        if(null != anno) {
                            return anno;
                        }
                        return CObjUtils.OBJECT;
                    });
            }
        }

        if(CObjUtils.OBJECT == annotation) {
            return null;
        }

        return CObjUtils.anyType(annotation);
    }

    /**
     * 获取类 注解
     * @param clazz 类
     * @param annotationClass 注解类
     * @return 注解
     * @param <T> 注解类泛型
     */
    public <T extends Annotation> T getAnnotationCached(Class<?> clazz, Class<T> annotationClass) {
        return getAnnotationCached(
            clazz,
            clazz::getAnnotation,
            annotationClass
        );
    }

    /**
     * 获取方法 注解
     * @param executable 方法
     * @param annotationClass 注解类
     * @return 注解
     * @param <T> 注解类泛型
     */
    public <T extends Annotation> T getAnnotationCached(Executable executable, Class<T> annotationClass) {
        return getAnnotationCached(
            executable,
            executable::getAnnotation,
            annotationClass
        );
    }

    /**
     * 获取字段 注解
     * @param field 字段
     * @param annotationClass 注解类
     * @return 注解
     * @param <T> 注解类泛型
     */
    public <T extends Annotation> T getAnnotationCached(Field field, Class<T> annotationClass) {
        return getAnnotationCached(
            field,
            field::getAnnotation,
            annotationClass
        );
    }

    /**
     * 获取类注解值
     * @param clazz 类
     * @param annotationClass 注解类
     * @param valueFunction 获取注解值的方法
     * @return 注解值
     * @param <A> 注解类泛型
     * @param <T> 返回值泛型
     */
    public <A extends Annotation, T> T getAnnotationValueCached(
        Class<?> clazz,
        Class<A> annotationClass,
        Function<A, T> valueFunction
    ) {

        val annotation = getAnnotationCached(clazz, annotationClass);
        if(null != annotation) {
            return valueFunction.apply(annotation);
        }
        return null;
    }

    /**
     * 获取方法注解值
     * @param executable 方法
     * @param annotationClass 注解类
     * @param valueFunction 获取注解值的方法
     * @return 注解值
     * @param <A> 注解类泛型
     * @param <T> 返回值泛型
     */
    public <A extends Annotation, T> T getAnnotationValueCached(
        Executable executable,
        Class<A> annotationClass,
        Function<A, T> valueFunction
    ) {

        val annotation = getAnnotationCached(executable, annotationClass);
        if(null != annotation) {
            return valueFunction.apply(annotation);
        }
        return null;
    }

    /**
     * 获取字段注解值
     * @param field 字段
     * @param annotationClass 注解类
     * @param valueFunction 获取注解值的方法
     * @return 注解值
     * @param <A> 注解类泛型
     * @param <T> 返回值泛型
     */
    public <A extends Annotation, T> T getAnnotationValueCached(
        Field field,
        Class<A> annotationClass,
        Function<A, T> valueFunction
    ) {

        val annotation = getAnnotationCached(field, annotationClass);
        if(null != annotation) {
            return valueFunction.apply(annotation);
        }
        return null;
    }

    @SneakyThrows
    public MethodHandle getMethodHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

}
