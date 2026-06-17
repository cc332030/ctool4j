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

    @SneakyThrows
    public MethodHandle toGetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

    final Cache<Field, MethodHandle> GETTER_HANDLE_CACHE = CLocalCacheUtils.buildCache();

    @SneakyThrows
    public MethodHandle getGetterHandle(Field field) {
        return GETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toGetterHandle);
    }

    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectSetter(field);
    }

    final Cache<Field, MethodHandle> SETTER_HANDLE_CACHE = CLocalCacheUtils.buildCache();

    @SneakyThrows
    public MethodHandle getSetterHandle(Field field) {
        return SETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toSetterHandle);
    }

    @SneakyThrows
    public MethodHandle toHandle(Method method) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflect(method);
    }

    final Cache<Method, MethodHandle> METHOD_HANDLE_CACHE = CLocalCacheUtils.buildCache();

    @SneakyThrows
    public MethodHandle getHandle(Method method) {
        return METHOD_HANDLE_CACHE.get(method, CMethodHandleUtils::toHandle);
    }

    @SneakyThrows
    public MethodHandle toHandleSpecial(Method method, Class<?> specialToken) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflectSpecial(method, specialToken);
    }

    @SneakyThrows
    public MethodHandle toHandle(Constructor<?> constructor) {
        constructor.setAccessible(true);
        return MethodHandles.lookup().unreflectConstructor(constructor);
    }

    final Cache<Constructor<?>, MethodHandle> CONSTRUCTOR_HANDLE_CACHE = CLocalCacheUtils.buildCache();

    @SneakyThrows
    public MethodHandle getHandle(Constructor<?> constructor) {
        return CONSTRUCTOR_HANDLE_CACHE.get(constructor, CMethodHandleUtils::toHandle);
    }

}
