package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CValueUtils
 * </p>
 *
 * @since 2025/12/4
 */
@UtilityClass
public class CValueUtils {

    /**
     * 通过函数取值后获取枚举值
     *
     * @param obj      对象
     * @param function 取值函数
     * @param <E>      对象类型
     * @param <T>      枚举值类型
     * @return 枚举值，对象或函数结果为 null 时返回 null
     */
    public <E, T> T getValue(E obj, CFunction<E, ICValue<T>> function) {
        val iValue = CObjUtils.convert(obj, function);
        return getValue(iValue);
    }

    /**
     * 获取枚举值
     *
     * @param iValue 枚举
     * @param <T>    枚举值类型
     * @return 枚举值，枚举为 null 时返回 null
     */
    public <T> T getValue(ICValue<T> iValue) {
        return CObjUtils.convert(iValue, ICValue::getValue);
    }

    /**
     * 枚举值消费（枚举为 null 时跳过）
     *
     * @param iValue   枚举
     * @param consumer 消费函数
     * @param <T>      枚举值类型
     */
    public <T> void setValue(ICValue<T> iValue, CConsumer<T> consumer) {
        if(null != iValue) {
            consumer.accept(iValue.getValue());
        }
    }

}
