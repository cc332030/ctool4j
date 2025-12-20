package com.c332030.ctool4j.core.classes;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CCollectors;
import com.c332030.ctool4j.core.util.CSet;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CClassUtils
 * </p>
 *
 * @since 2025/11/21
 */
@CustomLog
@UtilityClass
public class CClassUtils {

    public static final Set<String> BASE_PACKAGES = CSet.of(
        "java"
        , "javax"
        , "sun"
        , "com.sun"
        , "com.oracle"
        , "jdk"
    );

    public static final Set<String> BASE_PACKAGES_START = BASE_PACKAGES
        .stream()
        .map(e -> e + ".")
        .collect(Collectors.toSet());

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

    public String getFirstPackage(Class<?> clazz) {
        return clazz.getName().split("\\.")[0];
    }

    public boolean isBasicClass(Class<?> clazz) {
        return BASE_CLASSES.contains(clazz);
    }

    public boolean isJdkClass(Class<?> clazz) {

        if (BASE_CLASSES.contains(clazz)) {
            return true;
        }

        val className = clazz.getName();
        for (val basePackage : BASE_PACKAGES_START) {
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

        val fieldMap1 = CReflectUtils.getFields(class1);
        val fieldMap2 = CReflectUtils.getFields(class2);

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

}
