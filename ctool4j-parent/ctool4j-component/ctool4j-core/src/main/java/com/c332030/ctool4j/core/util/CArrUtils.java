package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CArrUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CArrUtils {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public <T> List<T> filter(T[] array, Predicate<T> predicate) {

        if(ArrayUtil.isEmpty(array)) {
            return CList.of();
        }

        return Arrays.stream(array)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public <T> List<T> filterNull(T[] array) {
        return filter(array, Objects::nonNull);
    }

    public <T> T get(T[] arr, int index) {

        if(ArrayUtil.isEmpty(arr)) {
            return null;
        }

        var newIndex = index;
        val length = arr.length;
        if(index < 0) {
            newIndex = length + index;
        }

        if(newIndex >= length) {
            return null;
        }

        return arr[newIndex];
    }

}
