package com.c332030.ctool4j.core.classes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
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
 * <h2>能力目录</h2>
 * <p>{@code CReflectUtils} 为反射工具类，提供：</p>
 * <ul>
 *   <li>缓存：{@code FIELD_MAP_CLASS_VALUE} / {@code INSTANCE_FIELD_MAP_CLASS_VALUE} / {@code ALL_METHODS_CLASS_VALUE} 等</li>
 *   <li>构造器：{@code getAllConstructors} / {@code getConstructors} / {@code getNoArgConstructor} / {@code newInstance}</li>
 *   <li>方法：{@code getMethods} / {@code getAllMethods} / {@code getAllMethodsCached} / {@code getAllMethodsByName}</li>
 *   <li>字段：{@code getAllFieldMap} / {@code getInstanceFieldMap} / {@code getField} / {@code getFieldMap}</li>
 *   <li>读写：{@code getValue} / {@code setValue}（按字段或字段名，MethodHandle 快速路径）</li>
 *   <li>调用：{@code invoke} / {@code invokeIgnoreNoMethod} / {@code invokeMustHaveMethod}（按方法名调用，MethodHandle）</li>
 *   <li>填充：{@code fillValues}（按字段值 Map 创建/填充对象，字段写入经 {@code setValue}）</li>
 *   <li>方法句柄：{@code getGetterHandleMap} / {@code getSetterHandleMap}</li>
 *   <li>注解：{@code getAnnotationCached} / {@code getFieldName}</li>
 *   <li>判断：{@code isStatic} / {@code isFinal}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>按字段名读写不存在字段</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>final 字段 setValue / fillValues</td>
 *     <td>回退 Field.set（handle 缓存排除 final 字段）</td>
 *   </tr>
 *   <tr>
 *     <td>静态字段读写</td>
 *     <td>回退 Field 原生路径</td>
 *   </tr>
 *   <tr>
 *     <td>调用不存在的方法（{@code ignoreNoMethod} 为 false）</td>
 *     <td>抛 IllegalStateException</td>
 *   </tr>
 *   <tr>
 *     <td>实参类型不匹配</td>
 *     <td>抛 ClassCastException（原生反射为 IllegalArgumentException），见「已知限制与取舍」</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>反射获取字段/方法/构造器、字段快速读写、方法/构造器调用、注解查询。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>按字段名读写不存在字段抛异常，调用方需预校验。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>实例字段走 MethodHandle 快速路径提升性能，final/静态字段回退保证兼容。</li>
 *   <li>实参类型不匹配时抛 ClassCastException（原生 Field.set / Method.invoke 抛 IllegalArgumentException）：
 *   句柄调用按签名做类型适配，不额外做类型校验，为性能取舍；调用方需自行保证实参类型。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>缓存</b></p>
 * <ul>
 *   <li>各类元数据（字段/方法/方法句柄）按类缓存于 {@code CClassValue}，避免重复反射。</li>
 * </ul>
 * <p><b>字段读写（getValue/setValue）</b></p>
 * <ul>
 *   <li>实例字段走缓存的 MethodHandle 快速路径；静态字段回退 Field 原生路径。</li>
 *   <li>final 字段 setValue 回退 Field.set（handle 缓存排除 final 字段）。</li>
 *   <li>父类声明字段可经子类实例读写；按字段名读写时字段不存在快速失败（抛 IllegalArgumentException）。</li>
 * </ul>
 * <p><b>方法/构造器调用（invoke/newInstance/fillValues）</b></p>
 * <ul>
 *   <li>方法调用与构造器实例化统一经 {@link CMethodHandleUtils} 的方法/构造器句柄做变长实参调用
 *   （不再使用 {@code Method#invoke}、{@code Constructor#newInstance}）；句柄生成时统一 {@code setAccessible}，
 *   私有成员亦可直接调用。</li>
 *   <li>实例方法句柄以接收者为首参（{@code bindTo} 绑定接收者），静态方法句柄不带接收者。</li>
 *   <li>句柄获取按调用频次选择：多次访问用缓存版 {@code getHandle}（弱 key 缓存，命中免 unreflect）；
 *   一次性（句柄由调用方长期持有）用生成版 {@code toHandle}。判定规则见 {@link CMethodHandleUtils} 「API 选用」。</li>
 *   <li>{@code fillValues} 的字段写入统一委托 {@link #setValue(Object, Field, Object, boolean)}，
 *   与单字段写入同一路径（非 final 走 setter 句柄，final 按既有约定回退）。</li>
 * </ul>
 *
 * @since 2024/4/2
 * @version 1.1
 * @see "doc/design/core/method-handle.adoc"
 */
@CustomLog
@UtilityClass
public class CReflectUtils {

    /**
     * 字段名到字段的缓存
     */
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

    /**
     * 实例字段（非静态）名到字段的缓存
     */
    public static final CClassValue<Map<String, Field>> INSTANCE_FIELD_MAP_CLASS_VALUE =
            CClassValue.of(type -> CClassUtils.getMap(
                    type,
                    Class::getDeclaredFields,
                    field -> !CReflectUtils.isStatic(field),
                    Field::getName,
                    field -> {
                        field.setAccessible(true);
                        return field;
                    }
            ));

    /**
     * 实例字段 getter MethodHandle 缓存（非静态字段，统一 Object 签名，供 invokeExact 快速路径直接调用）
     */
    public static final CClassValue<Map<String, MethodHandle>> GETTER_HANDLE_MAP_CLASS_VALUE =
            CClassValue.of(type -> CClassUtils.getMap(
                    type,
                    Class::getDeclaredFields,
                    field -> !CReflectUtils.isStatic(field),
                    Field::getName,
                    CMethodHandleUtils::getGetterHandleAsType
            ));

    /**
     * 实例字段 setter MethodHandle 缓存（非静态、非 final 字段，统一 Object 签名）
     */
    public static final CClassValue<Map<String, MethodHandle>> SETTER_HANDLE_MAP_CLASS_VALUE =
            CClassValue.of(type -> CClassUtils.getMap(
                    type,
                    Class::getDeclaredFields,
                    field -> !CReflectUtils.isStatic(field) && !CReflectUtils.isFinal(field),
                    Field::getName,
                    CMethodHandleUtils::getSetterHandleAsType
            ));

    /**
     * 构造器按参数个数分组的缓存
     */
    private static final CClassValue<Map<Integer, List<Constructor<?>>>> CONSTRUCTOR_MAP_CLASS_VALUE =
            CClassValue.of(type -> {

                val constructors = type.getConstructors();
                for (val constructor : constructors) {
                    constructor.setAccessible(true);
                }
                return Arrays.stream(constructors)
                        .collect(Collectors.groupingBy(Constructor::getParameterCount));
            });

    /**
     * 获取类所有构造器（按参数个数分组）
     *
     * @param tClass 类
     * @return 构造器分组 Map
     */
    public Map<Integer, List<Constructor<?>>> getAllConstructors(Class<?> tClass) {
        return CONSTRUCTOR_MAP_CLASS_VALUE.get(tClass);
    }

    /**
     * 获取类与参数匹配的构造器
     *
     * @param tClass 类
     * @param args   实参
     * @return 匹配的构造器 List
     */
    public List<Constructor<?>> getConstructors(Class<?> tClass, Object... args) {

        val argTypes = CArrUtils.convert(args, Object::getClass);
        return getConstructors(tClass, argTypes);
    }

    /**
     * 获取类与参数类型匹配的构造器
     *
     * @param tClass   类
     * @param argTypes 参数类型
     * @return 匹配的构造器 List
     */
    public List<Constructor<?>> getConstructors(Class<?> tClass, Class<?>... argTypes) {

        val argsLength = ArrayUtil.length(argTypes);

        val constructorMap = CONSTRUCTOR_MAP_CLASS_VALUE.get(tClass);
        val constructors = constructorMap.get(argsLength);
        if(argsLength == 0) {
            return constructors;
        }

        // 无该参数个数的构造器时返回空列表，避免迭代 null 抛 NPE
        if(null == constructors) {
            return Collections.emptyList();
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

    /**
     * 获取类的无参构造器
     *
     * @param tClass 类
     * @return 无参构造器
     */
    public Constructor<?> getNoArgConstructor(Class<?> tClass) {

        val constructors = getConstructors(tClass);
        return CCollUtils.first(constructors);
    }

    /**
     * 通过无参构造器实例化对象
     * <p>构造器 MethodHandle 按类缓存（复用 {@link CMethodHandleUtils} 生成的 handle），
     * 消除热路径每次的构造器查表与弱 key 缓存查找开销</p>
     *
     * @param tClass 类
     * @param <T>    类型
     * @return 实例
     */
    @SneakyThrows
    public <T> T newInstance(Class<T> tClass) {
        return CObjUtils.anyType(NO_ARG_CONSTRUCTOR_HANDLE_CLASS_VALUE.get(tClass).invoke());
    }

    /**
     * 无参构造器 MethodHandle 缓存（按类）
     * <p>句柄经 {@code getHandle}（缓存版）获取：该无参构造器可能已由其他调用点
     * （如 {@link #newInstance(Constructor, Object...)}）生成并缓存同一句柄，命中即共享、免去重复 unreflect；
     * 本处按类只计算一次，弱 key 缓存查找不构成重复开销</p>
     */
    private static final CClassValue<MethodHandle> NO_ARG_CONSTRUCTOR_HANDLE_CLASS_VALUE =
            CClassValue.of(type -> {

                val noArgConstructor = getNoArgConstructor(type);
                CAssert.notNull(noArgConstructor, () -> " can't find no arg constructor, class: " + type);

                return CMethodHandleUtils.getHandle(noArgConstructor);
            });

    /**
     * 通过构造器实例化对象
     * <p>经 {@link CMethodHandleUtils} 的构造器方法句柄做变长实参调用（等价于按实参反射调用），
     * 不使用 {@code Constructor#newInstance}；句柄生成时统一 {@code setAccessible}，私有构造器亦可实例化</p>
     *
     * @param constructor 构造器
     * @param args        实参（为 null 视为无实参）
     * @param <T>         类型
     * @return 实例
     */
    @SneakyThrows
    public <T> T newInstance(Constructor<T> constructor, Object... args) {

        // 多次访问：同一构造器会被反复实例化，句柄按 Constructor 弱 key 缓存（命中即免去重复 unreflect）
        val handle = CMethodHandleUtils.getHandle(constructor);
        val invokerArgs = ObjectUtil.defaultIfNull(args, CArrUtils.EMPTY_OBJECT_ARRAY);
        return CObjUtils.anyType(handle.invokeWithArguments(invokerArgs));

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

    /**
     * 获取类及其父类的所有方法
     *
     * @param type 类
     * @return 方法 List
     */
    public List<Method> getAllMethods(Class<?> type) {
        return CClassUtils.getMap(type, Class::getDeclaredMethods);
    }

    /**
     * 类所有方法缓存
     */
    public static final CClassValue<List<Method>> ALL_METHODS_CLASS_VALUE =
        CClassValue.of(CReflectUtils::getAllMethods);

    /**
     * 获取类及其父类的所有方法（带缓存）
     *
     * @param type 类
     * @return 方法 List
     */
    public List<Method> getAllMethodsCached(Class<?> type) {
        return ALL_METHODS_CLASS_VALUE.get(type);
    }

    /**
     * 方法名到方法列表的缓存
     */
    public static final CClassValue<Map<String, List<Method>>> METHOD_MAP_CLASS_VALUE =
            CClassValue.of(type -> ALL_METHODS_CLASS_VALUE.get(type)
                    .stream()
                    .collect(Collectors.groupingBy(Method::getName))
            );

    /**
     * 获取类所有方法（按方法名分组）
     *
     * @param type 类
     * @return 方法名到方法列表的 Map
     */
    public Map<String, List<Method>> getAllMethodsMap(Class<?> type) {
        return METHOD_MAP_CLASS_VALUE.get(type);
    }

    /**
     * 获取类指定名称的所有方法
     *
     * @param type       类
     * @param methodName 方法名
     * @return 方法 List
     */
    public List<Method> getAllMethodsByName(Class<?> type, String methodName) {
        return getAllMethodsMap(type).get(methodName);
    }

    /**
     * 获取字段名称（优先使用注解值）
     *
     * @param field                  字段
     * @param annotationClass        注解类
     * @param annotationValueFunction 注解值获取函数
     * @param <T>                    注解类型
     * @return 字段名称
     */
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

    /**
     * 获取类及其父类的所有字段 Map
     *
     * @param type 类
     * @return 字段名到字段的 Map
     */
    public Map<String, Field> getAllFieldMap(Class<?> type) {
        return FIELD_MAP_CLASS_VALUE.get(type);
    }

    /**
     * 获取类及其父类满足条件的字段 Map
     *
     * @param type      类
     * @param predicate 过滤条件
     * @return 字段名到字段的 Map
     */
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
     * 获取类的实例变量 map（缓存，只读使用）
     * @param type 类
     * @return 实例变量 map
     */
    public Map<String, Field> getInstanceFieldMap(Class<?> type) {
        return INSTANCE_FIELD_MAP_CLASS_VALUE.get(type);
    }

    /**
     * 获取类实例字段 getter MethodHandle map（非静态字段，含 final，缓存）
     *
     * @param type 类
     * @return 字段名到 getter MethodHandle 的 map
     */
    public Map<String, MethodHandle> getGetterHandleMap(Class<?> type) {
        return GETTER_HANDLE_MAP_CLASS_VALUE.get(type);
    }

    /**
     * 获取类实例字段 setter MethodHandle map（非静态、非 final 字段，缓存）
     *
     * @param type 类
     * @return 字段名到 setter MethodHandle 的 map
     */
    public Map<String, MethodHandle> getSetterHandleMap(Class<?> type) {
        return SETTER_HANDLE_MAP_CLASS_VALUE.get(type);
    }

    /**
     * 获取类指定名称的字段
     *
     * @param type      类
     * @param fieldName 字段名
     * @return 字段
     */
    public Field getField(Class<?> type, String fieldName) {
        return Optional.ofNullable(getAllFieldMap(type).get(fieldName))
                .orElseThrow(() -> new IllegalArgumentException(type + " no field with name: " + fieldName));
    }

    /**
     * 判断字段是否为静态
     *
     * @param field 字段
     * @return 是否为静态
     */
    public boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断字段是否为 final
     *
     * @param field 字段
     * @return 是否为 final
     */
    public boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 判断方法是否为静态
     *
     * @param field 方法
     * @return 是否为静态
     */
    public boolean isStatic(Method field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断方法是否为 final
     *
     * @param field 方法
     * @return 是否为 final
     */
    public boolean isFinal(Method field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 获取对象指定字段名的值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @param <T>       值类型
     * @return 字段值
     * @throws IllegalArgumentException 字段不存在时抛出
     */
    public <T> T getValue(Object object, String fieldName) {
        return getValue(object, getField(object.getClass(), fieldName));
    }

    /**
     * 获取对象指定字段的值
     *
     * @param object 对象
     * @param field  字段
     * @param <T>    值类型
     * @return 字段值
     */
    public <T> T getValue(Object object, Field field) {
        return getValue(object, field, false);
    }

    /**
     * 获取对象指定字段的值
     * <p>非静态字段使用缓存的 MethodHandle 快速路径，静态字段回退 Field.get；
     * 类型不匹配时抛 ClassCastException（Field.get 抛 IllegalArgumentException），为性能取舍</p>
     *
     * @param object     对象
     * @param field      字段
     * @param accessible 字段是否已可访问
     * @param <T>        值类型
     * @return 字段值
     */
    @SneakyThrows
    public <T> T getValue(Object object, Field field, boolean accessible) {
        if (!accessible) {
            field.setAccessible(true);
        }
        val handle = GETTER_HANDLE_MAP_CLASS_VALUE.get(field.getDeclaringClass()).get(field.getName());
        if (null == handle) {
            return CObjUtils.anyType(field.get(object));
        }
        return CObjUtils.anyType(handle.invoke(object));
    }

    /**
     * 设置对象指定字段名的值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @param value     值
     * @throws IllegalArgumentException 字段不存在时抛出
     */
    public void setValue(Object object, String fieldName, Object value) {
        setValue(object, getField(object.getClass(), fieldName), value, true);
    }

    /**
     * 设置对象指定字段的值
     *
     * @param object 对象
     * @param field  字段
     * @param value  值
     */
    public void setValue(Object object, Field field, Object value) {
        setValue(object, field, value, false);
    }

    /**
     * 设置对象指定字段的值
     * <p>非静态非 final 字段使用缓存的 MethodHandle 快速路径，静态/final 字段回退 Field.set；
     * 类型不匹配时抛 ClassCastException（Field.set 抛 IllegalArgumentException），为性能取舍</p>
     *
     * @param object     对象
     * @param field      字段
     * @param value      值
     * @param accessible 字段是否已可访问
     */
    @SneakyThrows
    public void setValue(Object object, Field field, Object value, boolean accessible) {
        if (!accessible) {
            field.setAccessible(true);
        }
        val handle = SETTER_HANDLE_MAP_CLASS_VALUE.get(field.getDeclaringClass()).get(field.getName());
        if (null == handle) {
            field.set(object, value);
            return;
        }
        handle.invoke(object, value);
    }

    /**
     * 按字段值 Map 创建对象并填充
     *
     * @param clazz  类
     * @param fields 字段值 Map
     * @param <T>    类型
     * @return 填充后的对象，字段 Map 为空时返回 null
     */
    public <T> T fillValues(Class<T> clazz, Map<String, ?> fields) {

        if (MapUtil.isEmpty(fields)) {
            return null;
        }

        val object = newInstance(clazz);
        fillValues(object, fields);
        return object;
    }

    /**
     * 按字段值 Map 填充对象
     * <p>字段写入统一委托 {@link #setValue(Object, Field, Object, boolean)}（非 final 字段走缓存的 setter 句柄快速路径，
     * final 字段按既有约定回退），不再直接 {@code Field#set}；字段名在实例字段中不存在时跳过、不报错</p>
     *
     * @param object        对象
     * @param fieldValueMap 字段值 Map
     */
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
            // 经 setValue 统一写入：非 final 字段走缓存的 setter 句柄快速路径；字段来自实例字段缓存，已 setAccessible 故传 true
            setValue(object, field, value, true);
        }

    }

    /**
     * get 方法名转 set 方法名
     *
     * @param getMethodName get 方法名
     * @return set 方法名
     */
    public String toSetMethodName(String getMethodName) {
        return "s" + getMethodName.substring(1);
    }

    /**
     * set 方法名转 get 方法名
     *
     * @param setMethodName set 方法名
     * @return get 方法名
     */
    public String toGetMethodName(String setMethodName) {
        return "g" + setMethodName.substring(1);
    }

    /**
     * 调用对象方法（方法不存在时忽略）
     *
     * @param value      对象
     * @param methodName 方法名
     * @param args       实参
     * @param <T>        返回值类型
     * @return 方法返回值，方法不存在时返回 null
     */
    public <T> T invokeIgnoreNoMethod(Object value, String methodName, Object... args) {
        return invoke(value, methodName, true, args);
    }

    /**
     * 调用对象方法（方法必须存在）
     *
     * @param value      对象
     * @param methodName 方法名
     * @param args       实参
     * @param <T>        返回值类型
     * @return 方法返回值
     */
    public <T> T invokeMustHaveMethod(Object value, String methodName, Object... args) {
        return invoke(value, methodName, false, args);
    }

    /**
     * 调用对象方法
     * <p>经 {@link CMethodHandleUtils} 的方法句柄做变长实参调用（等价于按实参反射调用），
     * 不使用 {@code Method#invoke}；实例方法句柄以接收者为首参（{@code bindTo} 绑定），静态方法句柄不带接收者</p>
     *
     * @param value          对象
     * @param methodName     方法名
     * @param ignoreNoMethod 方法不存在时是否忽略
     * @param args           实参（为 null 视为无实参）
     * @param <T>            返回值类型
     * @return 方法返回值
     * @throws IllegalStateException 方法不存在且 {@code ignoreNoMethod} 为 false 时抛出
     * @throws ClassCastException    实参类型与形参不匹配时抛出（原生反射为 IllegalArgumentException，见「已知限制与取舍」）
     */
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

        // 多次访问：按方法名反复调用（业务侧循环/逐字段处理），句柄按 Method 弱 key 缓存；
        // 实例方法句柄须绑定接收者，bindTo 每次新建——接收者逐次不同，绑定句柄不可缓存（缓存会持有接收者引用）
        val handle = CMethodHandleUtils.getHandle(method);
        var invoker = handle;
        if (!isStatic(method)) {
            invoker = handle.bindTo(value);
        }
        val invokerArgs = ObjectUtil.defaultIfNull(args, CArrUtils.EMPTY_OBJECT_ARRAY);
        return CObjUtils.anyType(invoker.invokeWithArguments(invokerArgs));

    }

    /**
     * 方法/字段/类 注解缓存
     * <p>key 使用弱引用（Caffeine weakKeys）：避免强引用持有 Class/Method/Field 导致
     * 类加载器无法回收（热部署、动态类场景内存滞留）</p>
     */
    final Cache<Object, Map<Class<? extends Annotation>, Object>> ELEMENT_ANNOTATION_CACHE =
            Caffeine.newBuilder()
                .weakKeys()
                .build();

    /**
     * 获取方法/字段/类 注解
     * @param element 元素
     * @param getAnnoFunction 注解获取函数（返回 null 表示无注解）
     * @param annotationClass 注解类
     * @return 注解
     * @param <ELEMENT> 元素类型
     * @param <T> 注解类泛型
     */
    public <ELEMENT, T extends Annotation> T getAnnotationCached(
        ELEMENT element,
        Function<Class<T>, Annotation> getAnnoFunction,
        Class<T> annotationClass
    ) {

        var annotationMap = ELEMENT_ANNOTATION_CACHE.get(element, k -> new ConcurrentHashMap<>());

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

}
