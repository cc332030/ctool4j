package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * <p>
 * Description: CLambdaUtils
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
@UtilityClass
public class CLambdaUtils {

    /**
     * JDK 9+需注意模块访问权限，这里兼容JDK 8
     */
    final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * 获取指定类的 Lookup
     *
     * @param clazz 类
     * @return Lookup
     */
    public MethodHandles.Lookup getLookup(Class<?> clazz) {
        return LOOKUP.in(clazz);
    }

    /**
     * 获取字段 getter 方法句柄
     *
     * @param clazz 类
     * @param field 字段
     * @return getter 方法句柄
     */
    @SneakyThrows
    public MethodHandle getGetterMethodHandle(Class<?> clazz, Field field) {
        return getLookup(clazz).unreflectGetter(field);
    }

    /**
     * 获取字段 setter 方法句柄
     *
     * @param clazz 类
     * @param field 字段
     * @return setter 方法句柄
     */
    @SneakyThrows
    public MethodHandle getSetterMethodHandle(Class<?> clazz, Field field) {
        return getLookup(clazz).unreflectSetter(field);
    }

    /**
     * 根据方法句柄生成 Lambda 实例
     *
     * @param methodHandle    方法句柄
     * @param lambdaClass     Lambda 接口类
     * @param lambdaMethodName Lambda 接口的方法名
     * @param <T>             Lambda 类型
     * @return Lambda 实例
     */
    @SneakyThrows
    public <T> T getLambda(
            MethodHandle methodHandle,
            Class<T> lambdaClass,
            String lambdaMethodName
    ) {

        val callSite = LambdaMetafactory.metafactory(
                LOOKUP,
                lambdaMethodName, // 函数式接口的方法名（Getter继承Function，方法名是apply）
                MethodType.methodType(lambdaClass), // 生成的 Lambda 类型（Getter<T,R>）
                methodHandle.type().generic(), // 方法类型的泛型签名（T -> R）
                methodHandle, // 实际执行的方法句柄（字段读取）
                methodHandle.type() // 实际方法类型（具体的T -> R，比如User -> String）
        );

        val invokeExact = callSite.getTarget().invokeExact();
        return CObjUtils.anyType(invokeExact);
    }

    /**
     * 获取字段读取 Lambda
     *
     * @param clazz 类
     * @param field 字段
     * @return 字段读取 Lambda
     */
    @SneakyThrows
    public CFunction<Object, Object> getFieldGetLambda(Class<?> clazz, Field field) {
        val lambda = getLambda(
                getGetterMethodHandle(clazz, field),
                CFunction.class,
                CFunction.APPLY
        );
        return CObjUtils.anyType(lambda);
    }

    /**
     * 获取字段写入 Lambda
     *
     * @param clazz 类
     * @param field 字段
     * @return 字段写入 Lambda
     */
    @SneakyThrows
    public CBiConsumer<Object, Object> getFieldSetLambda(Class<?> clazz, Field field) {
        val lambda = getLambda(
                getSetterMethodHandle(clazz, field),
                CBiConsumer.class,
                CBiConsumer.ACCEPT
        );
        return CObjUtils.anyType(lambda);

    }

}
