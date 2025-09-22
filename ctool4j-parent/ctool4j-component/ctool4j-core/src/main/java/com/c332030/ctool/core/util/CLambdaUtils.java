package com.c332030.ctool.core.util;

import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CLambdaUtils
 * </p>
 *
 * @since 2025/9/22
 */
@UtilityClass
public class CLambdaUtils {

    public <T> Function<?, T> emptyFunction() {
        return o -> null;
    }

    public <T> Supplier<T> emptySupplier() {
        return () -> null;
    }

}
