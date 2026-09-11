package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CCollectors;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.util.CSet;
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
 * <h2>能力目录</h2>
 * <p>{@code CClassUtils} 为类工具类，提供：</p>
 * <ul>
 *   <li>判断：{@code isJdkClass} / {@code isBasicClass} / {@code isExistClass} / {@code isAnnotationPresent}（字段/方法/类）</li>
 *   <li>包名：{@code getFirstPackage} / {@code BASE_PACKAGES}</li>
 *   <li>继承链：{@code getSuperClasses} / {@code getInterfaces}</li>
 *   <li>层级元素：{@code getMap} / {@code getMap}（List）</li>
 *   <li>包扫描：{@code findClasses} / {@code listSubClass} / {@code listAnnotatedClass} / {@code listAnnotatedClassThenDo}</li>
 *   <li>字段对比：{@code compareField}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>isAnnotationPresent(字段/方法为 null)</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>getSuperClasses(Object)</td>
 *     <td>返回仅含 Object 自身</td>
 *   </tr>
 *   <tr>
 *     <td>getSuperClasses(接口)</td>
 *     <td>返回仅含接口自身</td>
 *   </tr>
 *   <tr>
 *     <td>isJdkClass</td>
 *     <td>基本类型或基础包前缀匹配</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>JDK 类判断、继承链/接口遍历、包下子类/注解类扫描。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>getInterfaces 不递归接口继承，仅收集直接实现接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>isJdkClass 按类缓存，消除热路径前缀匹配开销。</li>
 *   <li>getInterfaces 去重保序（LinkedHashSet），按父类链由子至父。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>继承链</b></p>
 * <ul>
 *   <li>{@code getSuperClasses}：do-while 收集类及其父类（不含 Object），顺序由子至父；Object 自身仅返回自身；</li>
 *   <li>接口无父类仅返回接口自身。</li>
 *   <li>{@code getInterfaces}：按父类链由子至父遍历各层直接接口，LinkedHashSet 去重保序；<b>不递归接口继承</b></li>
 *   <li>（仅收集直接实现接口）。</li>
 * </ul>
 * <p><b>包扫描</b></p>
 * <ul>
 *   <li>{@code findClasses}：基于 Spring {@code ClassPathScanningCandidateComponentProvider} + TypeFilter 扫描包下类。</li>
 *   <li>{@code listSubClass} / {@code listAnnotatedClass}：分别用 AssignableTypeFilter / AnnotationTypeFilter。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CClassUtils {

    /**
     * 基础包名集合
     */
    public static final Set<String> BASE_PACKAGES = CSet.of(
        "java"
        , "javax"
        , "sun"
        , "com.sun"
        , "com.oracle"
        , "jdk"
    );

    /**
     * 基础包名集合（以包名加点为后缀，用于前缀匹配）
     */
    public static final Set<String> BASE_PACKAGES_START = BASE_PACKAGES
        .stream()
        .map(e -> e + ".")
        .collect(Collectors.toSet());

    /**
     * 基本数据类型集合
     */
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

    /**
     * 获取类的第一个包名
     *
     * @param clazz 类
     * @return 第一个包名
     */
    public String getFirstPackage(Class<?> clazz) {
        return clazz.getName().split("\\.")[0];
    }

    /**
     * 判断是否为基本数据类型
     *
     * @param clazz 类
     * @return 是否为基本数据类型
     */
    public boolean isBasicClass(Class<?> clazz) {
        return BASE_CLASSES.contains(clazz);
    }

    /**
     * 判断是否为 JDK 类
     * <p>结果按类缓存（类所属包名固定，判断结果不会变化），消除热路径每次
     * 类名前缀匹配开销</p>
     *
     * <h2>JDK 类判断（isJdkClass）</h2>
     * <ul>
     *   <li>结果按类缓存（{@code CClassValue}）：基本类型或类名以 {@code BASE_PACKAGES_START}（java./javax./sun./jdk. 等）开头</li>
     *   <li>即为 JDK 类。</li>
     * </ul>
     *
     * @param clazz 类
     * @return 是否为 JDK 类*/
    public boolean isJdkClass(Class<?> clazz) {
        return IS_JDK_CLASS_CLASS_VALUE.get(clazz);
    }

    /**
     * JDK 类判断缓存（按类）
     */
    private static final CClassValue<Boolean> IS_JDK_CLASS_CLASS_VALUE =
            CClassValue.of(CClassUtils::checkJdkClass);

    /**
     * 判断是否为 JDK 类（不缓存）
     *
     * @param clazz 类
     * @return 是否为 JDK 类
     */
    private static boolean checkJdkClass(Class<?> clazz) {

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

    /**
     * 获取类型各层级元素组成的 Map（名称转值）
     *
     * @param type   类型
     * @param getTArr 获取元素的数组
     * @param getName 获取元素名称
     * @param convert 元素转换函数
     * @param <T>    元素类型
     * @param <V>    值类型
     * @return 元素名称到值的 Map
     */
    public <T, V> Map<String, V> getMap(
            Class<?> type,
            Function<Class<?>, T[]> getTArr,
            Function<T, String> getName,
            Function<T, V> convert
    ) {
        return getMap(type, getTArr, Objects::nonNull, getName, convert);
    }

    /**
     * 获取类型各层级元素组成的 Map（名称转值）
     *
     * @param type     类型
     * @param getTArr  获取元素的数组
     * @param predicate 过滤条件
     * @param getName  获取元素名称
     * @param convert  元素转换函数
     * @param <T>      元素类型
     * @param <V>      值类型
     * @return 元素名称到值的 Map
     */
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

    /**
     * 获取类型各层级元素组成的 List
     *
     * @param type   类型
     * @param getTArr 获取元素的数组
     * @param <T>    元素类型
     * @return 元素 List
     */
    public <T> List<T> getMap(
            Class<?> type,
            Function<Class<?>, T[]> getTArr
    ) {
        return getMap(type, getTArr, Objects::nonNull);
    }

    /**
     * 获取类型各层级元素组成的 List
     *
     * @param type     类型
     * @param getTArr  获取元素的数组
     * @param predicate 过滤条件
     * @param <T>      元素类型
     * @return 元素 List
     */
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

    /**
     * 在指定包下查找符合条件的类
     *
     * @param typeFilter  类型过滤器
     * @param packageName 包名
     * @param <T>         类类型
     * @return 符合条件的类 List
     */
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

    /**
     * 在指定包下列出指定父类的子类
     *
     * @param superClass  父类
     * @param packageName 包名
     * @param <T>         类类型
     * @return 子类 List
     */
    public <T> List<Class<T>> listSubClass(Class<T> superClass, String packageName) {
        return findClasses(new AssignableTypeFilter(superClass), packageName);
    }

    /**
     * 在指定包下列出标注了指定注解的类
     *
     * @param annotationClass 注解类
     * @param packageName     包名
     * @param <T>             类类型
     * @return 标注注解的类 List
     */
    public <T> List<Class<T>> listAnnotatedClass(Class<? extends Annotation> annotationClass, String packageName) {
        return findClasses(
            new AnnotationTypeFilter(annotationClass, true, false),
            packageName
        );
    }

    /**
     * 在指定包下列出标注了指定注解的类并依次执行操作
     *
     * @param annotationClass 注解类
     * @param packageName     包名
     * @param consumer        对每个类的操作
     */
    public void listAnnotatedClassThenDo(
        Class<? extends Annotation> annotationClass,
        String packageName,
        CConsumer<Class<Object>> consumer
    ) {
        val classes = listAnnotatedClass(annotationClass, packageName);
        CCollUtils.forEach(classes, consumer);
    }

    /**
     * 比较多个类的字段差异
     *
     * @param classes 待比较的类
     */
    public void compareField(Class<?>... classes) {

        CMapUtils.compare(
            Arrays.asList(classes),
            Class::getSimpleName,
            CReflectUtils::getInstanceFieldMap,
            field -> field.getType().getSimpleName()
        );

    }

    /**
     * 获取类及其所有父类（不含 Object）
     *
     * @param tClass 类
     * @return 类及其父类 List
     */
    public List<Class<?>> getSuperClasses(Class<?> tClass) {

        val classes = new ArrayList<Class<?>>();

        var type = tClass;
        do {

            classes.add(type);
            type = type.getSuperclass();
        } while (null != type && type != Object.class);
        return classes;
    }

    /**
     * 获取类及其父类实现的所有接口
     *
     * @param tClass 类
     * @return 接口 Set
     */
    public Set<Class<?>> getInterfaces(Class<?> tClass) {
        return getSuperClasses(tClass).stream()
                .flatMap(e -> Arrays.stream(e.getInterfaces()))
                .collect(CCollectors.toLinkedSet());
    }

    /**
     * 判断字段是否标注了指定注解
     * <ul>
     *   <li>{@code isAnnotationPresent(Class)}：类自身或其直接接口标注注解即返回 true。</li>
     * </ul>
     *
     * @param field           字段
     * @param annotationClass 注解类
     * @return 是否标注了注解，字段为 null 时返回 false
     */
    public boolean isAnnotationPresent(Field field, Class<? extends Annotation> annotationClass) {

        if(null == field) {
            return false;
        }
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断方法是否标注了指定注解
     *
     * @param method          方法
     * @param annotationClass 注解类
     * @return 是否标注了注解，方法为 null 时返回 false
     */
    public boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass) {

        if(null == method) {
            return false;
        }
        return method.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断类是否标注了指定注解（含其接口）
     *
     * @param tClass          类
     * @param annotationClass 注解类
     * @return 是否标注了注解
     */
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
