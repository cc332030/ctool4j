package com.c332030.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Objects;
import java.util.function.Function;

/**
 * <p>
 * Description: CObjUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CObjUtils {

    public <O, T> T convert(O o, Function<O, T> function) {
        return convert(o, function, null);
    }

    public <O, T> T convert(O o, Function<O, T> function, T defaultValue) {

        if(Objects.isNull(o)) {
            return defaultValue;
        }

        val value = function.apply(o);
        if(Objects.nonNull(value)) {
            return value;
        }

        return defaultValue;
    }

}
