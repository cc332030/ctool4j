package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.core.function.CFunction;
import com.c332030.ctool4j.core.function.CSupplier;
import com.c332030.ctool4j.core.mapstruct.CMapStructConvert;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CClassUtils
 * </p>
 *
 * @since 2024/4/2
 */
@CustomLog
@UtilityClass
public class CClassUtils {

    public static final Set<String> BASE_PACKAGES = CSet.of(
        String.class.getName().split("\\.")[0]
    );

    public static final Set<Class<?>> BASE_CLASSES = CSet.of(
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class
    );

    public boolean isBasicClass(Class<?> clazz) {

        if(BASE_CLASSES.contains(clazz)) {
            return true;
        }

        val className = clazz.getName();
        for (val basePackage : BASE_PACKAGES) {
            if(className.startsWith(basePackage)) {
                return true;
            }
        }

        return false;
    }

    public static final ClassValue<Map<String, Field>> FIELD_MAP_CLASS_VALUE = new ClassValue<Map<String, Field>>() {
        @Override
        protected Map<String, Field> computeValue(@NonNull Class<?> type) {
            return CClassUtils.get(
                    type,
                    Class::getDeclaredFields,
                    field -> !CClassUtils.isStatic(field),
                    Field::getName,
                    field -> {
                        field.setAccessible(true);
                        return field;
                    }
            );
        }
    };

    public static final ClassValue<Constructor<?>> NO_ARGS_CONSTRUCTOR_MAP_CLASS_VALUE =
            new ClassValue<Constructor<?>>() {
                @Override
                protected Constructor<?> computeValue(@NonNull Class<?> type) {
                    return CSupplier.get(type::getConstructor);
                }
            };

    public static final ClassValue<Map<String, Method>> METHOD_MAP_CLASS_VALUE = new ClassValue<Map<String, Method>>() {
        @Override
        protected Map<String, Method> computeValue(@NonNull Class<?> type) {
            return CClassUtils.get(type, Class::getDeclaredMethods, Method::getName, method -> {
                method.setAccessible(true);
                return method;
            });
        }
    };

    public Map<String, Field> getFields(Class<?> type) {
        return FIELD_MAP_CLASS_VALUE.get(type);
    }

    public Field getField(Class<?> type, String fieldName) {
        return Optional.ofNullable(getFields(type).get(fieldName))
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

    public Map<String, Method> getMethods(Class<?> type) {
        return METHOD_MAP_CLASS_VALUE.get(type);
    }

    public <T, V> Map<String, V> get(
            Class<?> type,
            Function<Class<?>, T[]> getTArr,
            Function<T, String> getName,
            Function<T, V> convert
    ) {
        return get(type, getTArr, Objects::nonNull, getName, convert);
    }

    public <T, V> Map<String, V> get(
            Class<?> type,
            Function<Class<?>, T[]> getTArr,
            Predicate<T> predicate,
            Function<T, String> getName,
            Function<T, V> convert
    ) {

        val classes = getSuperClasses(type);

        // 覆盖父类方法
        Collections.reverse(classes);

        return classes.stream()
                .map(getTArr)
                .flatMap(Arrays::stream)
                .filter(predicate)
                .collect(Collectors.collectingAndThen(Collectors.toMap(
                        getName,
                        convert,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ), Collections::unmodifiableMap));
    }

    public <T> T getValue(Object object, String fieldName) {
        return getValue(object, getFields(object.getClass()).get(fieldName));
    }

    public <T> T getValue(Object object, Field field) {
        return getValue(object, field, false);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object object, Field field, boolean accessible) {
        if (!accessible) {
            field.setAccessible(true);
        }
        return (T) field.get(object);
    }

    public void setValue(Object object, String fieldName, Object value) {
        setValue(object, getFields(object.getClass()).get(fieldName), value, true);
    }

    @SneakyThrows
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

        if(MapUtil.isEmpty(fields)) {
            return null;
        }

        val object = newInstance(clazz);
        fillValues(object, fields);
        return object;
    }

    @SneakyThrows
    public void fillValues(Object object, Map<String, Object> fieldValueMap) {

        if(MapUtil.isEmpty(fieldValueMap)) {
            return;
        }

        val fieldMap = getFields(object.getClass());

        for(val entry : fieldValueMap.entrySet()) {

            val fieldName = entry.getKey();
            val value = entry.getValue();

            val field = fieldMap.get(fieldName);
            if(null == field) {
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
    @SuppressWarnings("unchecked")
    public <T> T invoke(Object value, String methodName, boolean ignoreNoMethod, Object... args) {

        Class<?> clazz = value.getClass();
        Method method = METHOD_MAP_CLASS_VALUE.get(clazz).get(methodName);
        if (null == method) {
            if (ignoreNoMethod) {
                return null;
            }
            throw new IllegalStateException("can't find method: " + methodName + " in class: " + clazz);
        }

        return (T) method.invoke(value, args);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> listSubClass(Class<T> superClass, String packageName) {

        val provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(superClass));

        val components = provider.findCandidateComponents(packageName);
        return components.stream()
                .map(beanDefinition -> {
                    try {
                        return (Class<T>) Class.forName(beanDefinition.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        log.debug("can't find subclass for: {}", superClass, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> getConstructor(Class<T> tClass) {
        return (Constructor<T>)NO_ARGS_CONSTRUCTOR_MAP_CLASS_VALUE.get(tClass);
    }

    @SneakyThrows
    public <T> T newInstance(Class<T> tClass) {
        return getConstructor(tClass).newInstance();
    }

    public void compareField(Class<?> class1, Class<?> class2) {

        log.info("class1: {}", class1);
        log.info("class2: {}", class2);

        val fieldMap1 = FIELD_MAP_CLASS_VALUE.get(class1);
        val fieldMap2 = FIELD_MAP_CLASS_VALUE.get(class2);

        log.info("\n");
        val commonFieldNames = new LinkedHashSet<>(fieldMap1.keySet());
        commonFieldNames.retainAll(fieldMap2.keySet());
        log.info("commonFields.size: {}", commonFieldNames::size);
        if(CollUtil.isNotEmpty(commonFieldNames)) {

            val typeMatchFieldNames = new ArrayList<>();
            val typeMismatchFieldNames = new ArrayList<>();

            commonFieldNames.forEach(fieldName -> {

                if(fieldMap1.get(fieldName).getType().equals(fieldMap2.get(fieldName).getType())) {
                    typeMatchFieldNames.add(fieldName);
                } else {
                    typeMismatchFieldNames.add(fieldName);
                }
            });

            typeMatchFieldNames.forEach(fieldName -> log.info("match name: {}, type: {}",
                    fieldName, fieldMap1.get(fieldName).getType()));

            typeMismatchFieldNames.forEach(fieldName -> log.info("mismatch name: {}, type1: {}, type2: {}",
                    fieldName, fieldMap1.get(fieldName).getType(), fieldMap2.get(fieldName).getType()));

        }


        log.info("\n");
        val leftFieldNames = new LinkedHashSet<>(fieldMap1.keySet());
        leftFieldNames.removeAll(fieldMap2.keySet());
        log.info("leftFields.size: {}", leftFieldNames::size);

        leftFieldNames.forEach(name -> log.info("left name: {}, type: {}",
                name, fieldMap1.get(name).getType()));


        log.info("\n");
        val rightFieldNames = new LinkedHashSet<>(fieldMap2.keySet());
        rightFieldNames.removeAll(fieldMap1.keySet());
        log.info("rightFields.size: {}", rightFieldNames::size);

        rightFieldNames.forEach(name -> log.info("right name: {}, type: {}",
                name, fieldMap2.get(name).getType()));

    }

    public List<Class<?>> getSuperClasses(Class<?> tClass) {

        val classes = new ArrayList<Class<?>>();

        var type = tClass;
        do {

            classes.add(type);
            type = type.getSuperclass();
        } while (null != type && type != Object.class);
        return classes;
    }

    public Set<Class<?>> getInterfaces(Class<?> tClass) {
        return getSuperClasses(tClass).stream()
                .flatMap(e -> Arrays.stream(e.getInterfaces()))
                .collect(CCollectors.toLinkedSet());
    }

    public boolean isAnnotationPresent(Class<?> tClass, Class<? extends Annotation> annotationClass) {

        if(tClass.isAnnotationPresent(annotationClass)) {
            return true;
        }

        val interfaces = getInterfaces(tClass);
        for (Class<?> iClass : interfaces) {
            if(iClass.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }

        return false;
    }


    public static final Map<Class<?>, Map<Class<?>, CFunction<?, ?>>> BEAN_CONVERTER_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <From, To> CFunction<From, To> getConverter(Class<From> fromClass, Class<To> toClass) {

        val converterMap = BEAN_CONVERTER_MAP.get(fromClass);
        if(null == converterMap) {
            return null;
        }

        return (CFunction<From, To>) converterMap.get(toClass);
    }

    public <From, To> boolean hasConverter(Class<From> fromClass, Class<To> toClass) {
        return null != getConverter(fromClass, toClass);
    }

    public static <From, To> void addConverter(
            Class<From> fromClass,
            Class<To> toClass,
            CFunction<From, To> converter) {
        log.trace("添加映射，fromClass: {}, toClass: {}, converter: {}", fromClass, toClass, converter);
        BEAN_CONVERTER_MAP.computeIfAbsent(fromClass, k -> new ConcurrentHashMap<>())
                .put(toClass, converter);
    }

    @SuppressWarnings("unchecked")
    public static void addConverter(Method method) {

        val fromClass = (Class<Object>)method.getParameterTypes()[0];
        val toClass = (Class<Object>)method.getReturnType();
        addConverter(fromClass, toClass, o -> method.invoke(null, o));
    }

    static {
        log.info("初始化 mapstruct 默认类型转换");
        val methods = getMethods(CMapStructConvert.class);
        methods.values()
                .stream()
                .filter(CClassUtils::isStatic)
                .forEach(CClassUtils::addConverter);
    }

}
