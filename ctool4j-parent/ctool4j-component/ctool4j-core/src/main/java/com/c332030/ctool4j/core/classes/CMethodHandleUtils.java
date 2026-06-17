package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.util.CMapUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

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

    final ConcurrentHashMap<Field, MethodHandle> GETTER_HANDLE_CACHE = new ConcurrentHashMap<>();

    public MethodHandle getGetterHandle(Field field) {
        return CMapUtils.computeIfAbsent(
            GETTER_HANDLE_CACHE,
            field,
            CMethodHandleUtils::toGetterHandle
        );
    }

    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectSetter(field);
    }

    final ConcurrentHashMap<Field, MethodHandle> SETTER_HANDLE_CACHE = new ConcurrentHashMap<>();

    public MethodHandle getSetterHandle(Field field) {
        return CMapUtils.computeIfAbsent(
            SETTER_HANDLE_CACHE,
            field,
            CMethodHandleUtils::toSetterHandle
        );
    }

    @SneakyThrows
    public MethodHandle toHandle(Method method) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflect(method);
    }


    final ConcurrentHashMap<Method, MethodHandle> METHOD_HANDLE_CACHE = new ConcurrentHashMap<>();

    public MethodHandle getHandle(Method method) {
        return CMapUtils.computeIfAbsent(
            METHOD_HANDLE_CACHE,
            method,
            CMethodHandleUtils::toHandle
        );
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

    final ConcurrentHashMap<Constructor<?>, MethodHandle> CONSTRUCTOR_HANDLE_CACHE = new ConcurrentHashMap<>();

    public MethodHandle getHandle(Constructor<?> constructor) {
        return CMapUtils.computeIfAbsent(
            CONSTRUCTOR_HANDLE_CACHE,
            constructor,
            CMethodHandleUtils::toHandle
        );
    }

}
