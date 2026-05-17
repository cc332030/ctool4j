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

    public boolean isTrue(Boolean value) {
        return BooleanUtil.isTrue(value);
    }

    public boolean isNotTrue(Boolean value) {
        return !isTrue(value);
    }

    public boolean isFalse(Boolean value){
        return BooleanUtil.isFalse(value);
    }

    public boolean isNotFalse(Boolean value){
        return !isFalse(value);
    }

    public <T> boolean isTrue(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isTrue(value);
    }

    public <T> boolean isNotTrue(T t, CFunction<T, Boolean> function) {
        return !isTrue(t, function);
    }

    public <T> boolean isFalse(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isFalse(value);
    }

    public <T> boolean isNotFalse(T t, CFunction<T, Boolean> function) {
        return !isFalse(t, function);
    }

}
