package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
     */
    final Cache<Field, MethodHandle> GETTER_HANDLE_CACHE = CLocalCacheUtils.buildCache();

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
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectSetter(field);
    }

    /**
     * 字段 setter 方法句柄缓存
     */
    final Cache<Field, MethodHandle> SETTER_HANDLE_CACHE = CLocalCacheUtils.buildCache();

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
     */
    final Cache<Method, MethodHandle> METHOD_HANDLE_CACHE = CLocalCacheUtils.buildCache();

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
     */
    final Cache<Constructor<?>, MethodHandle> CONSTRUCTOR_HANDLE_CACHE = CLocalCacheUtils.buildCache();

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
