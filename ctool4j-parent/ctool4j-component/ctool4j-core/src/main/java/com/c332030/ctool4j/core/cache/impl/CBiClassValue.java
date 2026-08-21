package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICBiClassValue;
import com.c332030.ctool4j.definition.function.CBiFunction;

/**
 * <p>
 * Description: CBiClassValue
 * </p>
 *
 * @since 2025/11/20
 * @see doc/design/core/CBiClassValue.adoc
 * @see doc/design/core/CBiClassValueTests.adoc
 */
public class CBiClassValue<T> implements ICBiClassValue<T> {

    private final CClassValue<CClassValue<T>> classValue;

    private CBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        classValue = CClassValue.of(type1 ->
                CClassValue.of(type2 ->
                        function.apply(type1, type2)));
    }

    /**
     * 获取值
     * @param type1 类1
     * @param type2 类2
     * @return 值
     */
    @Override
    public T get(Class<?> type1, Class<?> type2) {
        return classValue.get(type1).get(type2);
    }

    /**
     * 移除缓存值，下次 get 时重新计算
     * @param type1 类1
     * @param type2 类2
     */
    public void remove(Class<?> type1, Class<?> type2) {
        classValue.get(type1).remove(type2);
    }

    /**
     * 创建 CBiClassValue
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CBiClassValue<T> of(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CBiClassValue<>(function);
    }

}
