package com.c332030.ctool4j.core.classes;

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

    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectSetter(field);
    }

    @SneakyThrows
    public MethodHandle toHandle(Method method) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflect(method);
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

}
