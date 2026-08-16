package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>
 * Description: CMethodHandleUtils
 * </p>
 *
 * @since 2026/6/17
 */
@UtilityClass
public class CMethodHandleUtils {

    /**
     * getter MethodHandle 的统一签名：以 Object 接收者取 Object 值
     * <p>运行期 invokeExact 无签名适配开销；原始类型字段由 asType 适配器自动装箱/拆箱</p>
     */
    public static final MethodType GETTER_HANDLE_TYPE = MethodType.methodType(Object.class, Object.class);

    /**
     * setter MethodHandle 的统一签名：以 Object 接收者写入 Object 值
     */
    public static final MethodType SETTER_HANDLE_TYPE = MethodType.methodType(void.class, Object.class, Object.class);

    /**
     * 生成字段 getter 方法句柄
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toGetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

    /**
     * 字段 getter 方法句柄缓存
     * key 弱引用，避免 Field 及其所属类加载器无法回收（与 CReflectUtils.ELEMENT_ANNOTATION_CACHE 一致）
     */
    final Cache<Field, MethodHandle> GETTER_HANDLE_CACHE = CLocalCacheUtils.<Field, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取字段 getter 方法句柄（带缓存）
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getGetterHandle(Field field) {
        return GETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toGetterHandle);
    }

    /**
     * 生成字段 setter 方法句柄
     * <p>final 字段 JDK 禁止 unreflectSetter（抛 IllegalAccessException），
     * 兜底用 {@link Field#set} 包装的句柄（实例 final 字段 setAccessible 后可写；
     * static final 字段不可写，由 Field.set 抛异常，行为与反射语义一致）</p>
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        if (Modifier.isFinal(field.getModifiers())) {
            return MethodHandles.insertArguments(FIELD_SET_METHOD_HANDLE, 0, field);
        }
        return MethodHandles.lookup().unreflectSetter(field);
    }

    /**
     * {@link Field#set} 的方法句柄（final 字段兜底用，签名 (Field, Object, Object)void）
     */
    @SneakyThrows
    private static MethodHandle createFieldSetMethodHandle() {
        return MethodHandles.lookup().unreflect(Field.class.getMethod("set", Object.class, Object.class));
    }

    private static final MethodHandle FIELD_SET_METHOD_HANDLE = createFieldSetMethodHandle();

    /**
     * 字段 setter 方法句柄缓存
     * key 弱引用，避免 Field 及其所属类加载器无法回收
     */
    final Cache<Field, MethodHandle> SETTER_HANDLE_CACHE = CLocalCacheUtils.<Field, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取字段 setter 方法句柄（带缓存）
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getSetterHandle(Field field) {
        return SETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toSetterHandle);
    }

    /**
     * 获取字段 getter 方法句柄（统一 Object 签名，带缓存）
     * <p>与 {@link #getGetterHandle(Field)} 的差异：此处返回 {@link #GETTER_HANDLE_TYPE} 适配后的句柄，
     * 供 invokeExact 快速路径直接调用；原始类型字段由 asType 适配器自动装箱/拆箱。
     * 自 {@link CReflectUtils} 迁入（返回 MethodHandle 的方法统一归本类）</p>
     *
     * @param field 字段
     * @return getter 方法句柄（统一 Object 签名）
     */
    public MethodHandle getGetterHandleAsType(Field field) {
        return getGetterHandle(field).asType(GETTER_HANDLE_TYPE);
    }

    /**
     * 获取字段 setter 方法句柄（统一 Object 签名，带缓存）
     * <p>同 {@link #getGetterHandleAsType(Field)} 的签名语义，setter 版本以 {@link #SETTER_HANDLE_TYPE} 适配</p>
     *
     * @param field 字段
     * @return setter 方法句柄（统一 Object 签名）
     */
    public MethodHandle getSetterHandleAsType(Field field) {
        return getSetterHandle(field).asType(SETTER_HANDLE_TYPE);
    }

    /**
     * 生成方法的方法句柄
     *
     * @param method 方法
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandle(Method method) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflect(method);
    }

    /**
     * 方法句柄缓存
     * key 弱引用，避免 Method 及其所属类加载器无法回收
     */
    final Cache<Method, MethodHandle> METHOD_HANDLE_CACHE = CLocalCacheUtils.<Method, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取方法的方法句柄（带缓存）
     *
     * @param method 方法
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getHandle(Method method) {
        return METHOD_HANDLE_CACHE.get(method, CMethodHandleUtils::toHandle);
    }

    /**
     * 生成 special 方法句柄
     *
     * @param method       方法
     * @param specialToken special 令牌（调用类）
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandleSpecial(Method method, Class<?> specialToken) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflectSpecial(method, specialToken);
    }

    /**
     * 生成构造器方法句柄
     *
     * @param constructor 构造器
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandle(Constructor<?> constructor) {
        constructor.setAccessible(true);
        return MethodHandles.lookup().unreflectConstructor(constructor);
    }

    /**
     * 构造器方法句柄缓存
     * key 弱引用，避免 Constructor 及其所属类加载器无法回收
     */
    final Cache<Constructor<?>, MethodHandle> CONSTRUCTOR_HANDLE_CACHE =
        CLocalCacheUtils.<Constructor<?>, MethodHandle>cacheBuilder()
            .weakKeys()
            .build();

    /**
     * 获取构造器方法句柄（带缓存）
     *
     * @param constructor 构造器
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getHandle(Constructor<?> constructor) {
        return CONSTRUCTOR_HANDLE_CACHE.get(constructor, CMethodHandleUtils::toHandle);
    }

}
