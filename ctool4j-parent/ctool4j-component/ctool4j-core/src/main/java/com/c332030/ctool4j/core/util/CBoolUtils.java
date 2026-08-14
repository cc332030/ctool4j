package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CBoolUtils
 * </p>
 *
 * @since 2025/12/22
 */
@UtilityClass
public class CBoolUtils {

    /**
     * 判断布尔值是否为 true
     *
     * @param value 布尔值
     * @return 是否为 true，null 视为 false
     */
    public boolean isTrue(Boolean value) {
        return BooleanUtil.isTrue(value);
    }

    /**
     * 判断布尔值是否不为 true
     *
     * @param value 布尔值
     * @return 是否不为 true
     */
    public boolean isNotTrue(Boolean value) {
        return !isTrue(value);
    }

    /**
     * 判断布尔值是否为 false
     *
     * @param value 布尔值
     * @return 是否为 false，null 视为 false
     */
    public boolean isFalse(Boolean value){
        return BooleanUtil.isFalse(value);
    }

    /**
     * 判断布尔值是否不为 false
     *
     * @param value 布尔值
     * @return 是否不为 false
     */
    public boolean isNotFalse(Boolean value){
        return !isFalse(value);
    }

    /**
     * 通过函数取值后判断是否为 true
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否为 true
     */
    public <T> boolean isTrue(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isTrue(value);
    }

    /**
     * 通过函数取值后判断是否不为 true
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否不为 true
     */
    public <T> boolean isNotTrue(T t, CFunction<T, Boolean> function) {
        return !isTrue(t, function);
    }

    /**
     * 通过函数取值后判断是否为 false
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否为 false
     */
    public <T> boolean isFalse(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isFalse(value);
    }

    /**
     * 通过函数取值后判断是否不为 false
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否不为 false
     */
    public <T> boolean isNotFalse(T t, CFunction<T, Boolean> function) {
        return !isFalse(t, function);
    }

}
