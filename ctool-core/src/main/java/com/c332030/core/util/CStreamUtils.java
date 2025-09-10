package com.c332030.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>
 * Description: CStreamUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CStreamUtils {

    public <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        val seen = new ConcurrentHashMap<Object, Boolean>();
        return t -> {

            val key = CObjUtils.convert(t, keyExtractor);
            if(key == null) {
                return false;
            }

            return seen.putIfAbsent(key, Boolean.TRUE) == null;
        };
    }

}
