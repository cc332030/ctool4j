package com.c332030.ctool4j.core.classes;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CCollectors;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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


    public <T, V> Map<String, V> getMap(
            Class<?> type,
            Function<Class<?>, T[]> getTArr,
            Function<T, String> getName,
            Function<T, V> convert
    ) {
        return getMap(type, getTArr, Objects::nonNull, getName, convert);
    }

    public <T, V> Map<String, V> getMap(
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

    public <T> List<T> getMap(
            Class<?> type,
            Function<Class<?>, T[]> getTArr
    ) {
        return getMap(type, getTArr, Objects::nonNull);
    }

    public <T> List<T> getMap(
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

    public <T> List<Class<T>> findClasses(TypeFilter typeFilter, String packageName) {

        val startMills = System.currentTimeMillis();
        try {

            val provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(typeFilter);

            val components = provider.findCandidateComponents(packageName);
            return components.stream()
                .map(beanDefinition -> {
                    try {
                        @SuppressWarnings("unchecked")
                        val clazz = (Class<T>) Class.forName(beanDefinition.getBeanClassName());
                        return clazz;
                    } catch (ClassNotFoundException e) {
                        log.debug("can't find class for:", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            val cost = System.currentTimeMillis() - startMills;
            log.info("find {} classes cost: {}", packageName, cost);
        }
    }

    public <T> List<Class<T>> listSubClass(Class<T> superClass, String packageName) {
        return findClasses(new AssignableTypeFilter(superClass), packageName);
    }

    public <T> List<Class<T>> listAnnotatedClass(Class<? extends Annotation> annotationClass, String packageName) {
        return findClasses(
            new AnnotationTypeFilter(annotationClass, true, false),
            packageName
        );
    }

    public void listAnnotatedClassThenDo(
        Class<? extends Annotation> annotationClass,
        String packageName,
        CConsumer<Class<Object>> consumer
    ) {
        val classes = listAnnotatedClass(annotationClass, packageName);
        CCollUtils.forEach(classes, consumer);
    }

    public void compareField(Class<?>... classes) {

        val fieldClassMap = new LinkedHashMap<String, Map<Class<?>, Field>>();

        val sb = new StringBuilder("\n");
        for (val aClass : classes) {
            sb.append(aClass.getName()).append("\n");

            val fieldMap = CReflectUtils.getAllFieldMap(aClass);
            fieldMap.forEach((fieldName, field) ->
                    fieldClassMap.computeIfAbsent(fieldName, k -> new HashMap<>())
                            .put(aClass, field));
        }

        val tables = new ArrayList<List<String>>(classes.length + 1);

        val fieldNameColumn = new ArrayList<String>(fieldClassMap.size() + 1);
        fieldNameColumn.add("");
        tables.add(fieldNameColumn);

        for (val clazz : classes) {
            val list = new ArrayList<String>(fieldClassMap.size() + 1);
            list.add(clazz.getSimpleName());
            tables.add(list);
        }

        fieldClassMap.forEach((fieldName, map) -> {

            fieldNameColumn.add(fieldName);
            for (int i = 0; i < classes.length; i++) {

                val className = classes[i];
                val columnList = tables.get(i + 1);

                val fieldType = Opt.ofNullable(map.get(className))
                        .map(Field::getType)
                        .map(Class::getSimpleName)
                        .orElse("-");
                columnList.add(fieldType);
            }

        });

        val columnWidthList = new ArrayList<Integer>();
        tables.forEach(columnList -> {

            val maxWidth = columnList.stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(0);
            columnWidthList.add(maxWidth);

        });

        val lineSize = tables.get(0).size();
        for (int i = 0; i < lineSize; i++) {
            for (int i1 = 0; i1 < tables.size(); i1++) {

                val columnList = tables.get(i1);
                val column = columnList.get(i);

                val width = columnWidthList.get(i1) + 2;

                String columnReal;
                if (i1 == 0) {
                    columnReal = StrUtil.fillAfter(column, ' ', width);
                } else {
                    columnReal = CStrUtils.fillSide(column, ' ', width);
                }

                sb.append(columnReal);
            }
            sb.append("\n");
        }

        System.out.println(sb);

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

    public boolean isAnnotationPresent(Field field, Class<? extends Annotation> annotationClass) {

        if(null == field) {
            return false;
        }
        return field.isAnnotationPresent(annotationClass);
    }

    public boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass) {

        if(null == method) {
            return false;
        }
        return method.isAnnotationPresent(annotationClass);
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
