package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * <p>
 * Description: CStreamUtils
 * </p>
 *
 * @since 2025/9/10
 * @see "doc/design/core/CStreamUtils.adoc"
 * @see "doc/design/core/CStreamUtilsTests.adoc"
 */
@UtilityClass
public class CStreamUtils {

    /**
     * 按 key 去重谓词（并发安全，key 为 null 的元素会被过滤）
     *
     * @param keyExtractor key 提取函数
     * @param <T>          元素类型
     * @return 去重谓词
     */
    public <T> Predicate<T> distinctByKey(CFunction<? super T, ?> keyExtractor) {

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
