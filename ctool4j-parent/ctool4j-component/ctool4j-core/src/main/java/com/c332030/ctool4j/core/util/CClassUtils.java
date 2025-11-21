package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.core.lang.CAssert;
import com.c332030.ctool4j.core.mapstruct.CMapStructConvert;
import com.c332030.ctool4j.core.model.ClassConverter;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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

        if (BASE_CLASSES.contains(clazz)) {
            return true;
        }

        val className = clazz.getName();
        for (val basePackage : BASE_PACKAGES) {
            if (className.startsWith(basePackage)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断类是否存在
     * @param className 类名
     * @return 结果
     */
    public boolean isExistClass(String className) {
        try {

            Class.forName(className);
            return true;
        } catch (Throwable e) {
            log.debug("check class exception", e);
        }
        return false;
    }

    public static final CClassValue<Map<String, Field>> FIELD_MAP_CLASS_VALUE =
            CClassValue.of(type -> CClassUtils.get(
                    type,
                    Class::getDeclaredFields,
                    field -> !CClassUtils.isStatic(field),
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

    public <T> List<T> get(
            Class<?> type,
            Function<Class<?>, T[]> getTArr
    ) {
        return get(type, getTArr, Objects::nonNull);
    }

    public <T> List<T> get(
            Class<?> type,
            Function<Class<?>, T[]> getTArr,
            Predicate<T> predicate
    ) {

        val classes = getSuperClasses(type);

        // 覆盖父类方法
        Collections.reverse(classes);
        return classes.stream()
                .map(getTArr)
                .flatMap(Arrays::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public <T> T getValue(Object object, String fieldName) {
        return getValue(object, getFields(object.getClass()).get(fieldName));
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
        setValue(object, getFields(object.getClass()).get(fieldName), value, true);
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

        val fieldMap = getFields(object.getClass());

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

    public void compareField(Class<?> class1, Class<?> class2) {

        log.info("class1: {}", class1);
        log.info("class2: {}", class2);

        val fieldMap1 = FIELD_MAP_CLASS_VALUE.get(class1);
        val fieldMap2 = FIELD_MAP_CLASS_VALUE.get(class2);

        log.info("\n");
        val commonFieldNames = new LinkedHashSet<>(fieldMap1.keySet());
        commonFieldNames.retainAll(fieldMap2.keySet());
        log.info("commonFields.size: {}", commonFieldNames::size);
        if (CollUtil.isNotEmpty(commonFieldNames)) {

            val typeMatchFieldNames = new ArrayList<>();
            val typeMismatchFieldNames = new ArrayList<>();

            commonFieldNames.forEach(fieldName -> {

                if (fieldMap1.get(fieldName).getType().equals(fieldMap2.get(fieldName).getType())) {
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

        if (tClass.isAnnotationPresent(annotationClass)) {
            return true;
        }

        val interfaces = getInterfaces(tClass);
        for (Class<?> iClass : interfaces) {
            if (iClass.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }

        return false;
    }

    private static final List<ClassConverter<?, ?>> CLASS_CONVERTERS = new CopyOnWriteArrayList<>();
    public static final CBiClassValue<CFunction<Object, ?>> VALUE_SET_CLASS_VALUE =
            CBiClassValue.of((fromClass, toClass) -> {

                if(Collection.class.isAssignableFrom(fromClass)
                        || Map.class.isAssignableFrom(fromClass)
                        || fromClass.isArray()
                ) {
                    return null;
                }

                if(toClass.isAssignableFrom(fromClass)) {
                    return CFunction.SELF;
                }

                for (val classConverter : CLASS_CONVERTERS) {
                    if(classConverter.getFromClass().isAssignableFrom(fromClass)
                            && classConverter.getToClass().isAssignableFrom(toClass)) {
                        return CObjUtils.anyType(classConverter.getConverter());
                    }
                }

                return null;
            });

    public static <To> CFunction<Object, To> getConverter(Class<?> fromClass, Class<To> toClass) {
        return CObjUtils.anyType(VALUE_SET_CLASS_VALUE.get(fromClass, toClass));
    }

    public <To> To convert(Object from, Class<To> toClass) {
        if(from == null) {
            return null;
        }

        val converter = getConverter(from.getClass(), toClass);
        if(null == converter) {
            return null;
        }
        return converter.apply(from);
    }

    public <To> Opt<To> convertOpt(Object from, Class<To> toClass) {
        return Opt.ofNullable(convert(from, toClass));
    }

    public static <From, To> void addConverter(
            Class<From> fromClass,
            Class<To> toClass,
            CFunction<From, To> converter) {

        log.debug("添加映射，fromClass: {}, toClass: {}, converter: {}", fromClass, toClass, converter);

        val classConverter = ClassConverter.<From, To>builder()
                .fromClass(fromClass)
                .toClass(toClass)
                .converter(converter)
                .build();
        CLASS_CONVERTERS.add(classConverter);
    }

    public static void addConverter(Method method) {

        @SuppressWarnings("unchecked")
        val fromClass = (Class<Object>) method.getParameterTypes()[0];
        @SuppressWarnings("unchecked")
        val toClass = (Class<Object>) method.getReturnType();
        addConverter(fromClass, toClass, o -> method.invoke(null, o));
    }

    static {

        log.info("初始化 mapstruct 默认类型转换");
        val methods = getMethods(CMapStructConvert.class);
        methods.stream()
                .filter(CClassUtils::isStatic)
                .forEach(CClassUtils::addConverter);
    }

}
