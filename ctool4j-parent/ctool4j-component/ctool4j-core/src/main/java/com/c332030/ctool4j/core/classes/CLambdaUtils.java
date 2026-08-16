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
     * @param methodHandle          直接方法句柄（须为 direct handle；类型适配由 LambdaMetafactory 内部完成，支持基本类型装箱/拆箱）
     * @param lambdaClass           Lambda 接口类
     * @param lambdaMethodName      Lambda 接口抽象方法名（如 CFunction 为 applyThrowable）
     * @param samMethodType         接口抽象方法的泛型签名（如 (Object)Object）
     * @param instantiatedMethodType 接口方法实例化后的具体类型
     * @param <T>                   Lambda 类型
     * @return Lambda 实例
     */
    @SneakyThrows
    public <T> T getLambda(
            MethodHandle methodHandle,
            Class<T> lambdaClass,
            String lambdaMethodName,
            MethodType samMethodType,
            MethodType instantiatedMethodType
    ) {

        val callSite = LambdaMetafactory.metafactory(
                LOOKUP,
                lambdaMethodName, // 函数式接口的抽象方法名
                MethodType.methodType(lambdaClass), // 生成的 Lambda 类型
                samMethodType, // 接口抽象方法的泛型签名
                methodHandle, // 直接方法句柄（不能用 asType 包装，否则非 direct handle 导致 metafactory 抛异常）
                instantiatedMethodType // 接口方法实例化后的具体类型
        );

        val invokeExact = callSite.getTarget().invokeExact();
        return CObjUtils.anyType(invokeExact);
    }

    /**
     * 获取字段读取 Lambda
     * <p>CFunction 的抽象方法为 applyThrowable（apply 为 default 覆写），须以抽象方法名生成。</p>
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
                "applyThrowable",
                MethodType.methodType(Object.class, Object.class),
                MethodType.methodType(Object.class, Object.class)
        );
        return CObjUtils.anyType(lambda);
    }

    /**
     * 获取字段写入 Lambda
     * <p>CBiConsumer 的抽象方法为 acceptThrowable（accept 为 default 覆写），须以抽象方法名生成。</p>
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
                "acceptThrowable",
                MethodType.methodType(void.class, Object.class, Object.class),
                MethodType.methodType(void.class, Object.class, Object.class)
        );
        return CObjUtils.anyType(lambda);

    }

}
