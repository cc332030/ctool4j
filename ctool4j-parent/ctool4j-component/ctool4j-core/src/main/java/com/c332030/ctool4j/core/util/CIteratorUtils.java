package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Iterator;

/**
 * <p>
 * Description: CIteratorUtils
 * </p>
 *
 * @since 2025/9/28
 * @see "doc/design/core/CIteratorUtils.adoc"
 * @see "doc/design/core/CIteratorUtilsTests.adoc"
 */
@CustomLog
@UtilityClass
public class CIteratorUtils {

    /**
     * 遍历集合并忽略异常执行消费
     *
     * @param iterable 集合
     * @param consumer 消费函数
     * @param <T>      元素类型
     */
    public <T> void forEachIgnoreException(Iterable<T> iterable, CConsumer<T> consumer) {

        if(iterable == null) {
            return;
        }

        forEachIgnoreException(iterable.iterator(), consumer);

    }

    /**
     * 遍历迭代器并忽略异常执行消费（跳过 null 元素）
     *
     * @param iterator 迭代器
     * @param consumer 消费函数
     * @param <T>      元素类型
     */
    public <T> void forEachIgnoreException(Iterator<T> iterator, CConsumer<T> consumer) {

        if(iterator == null) {
            return;
        }

        while (iterator.hasNext()) {

            try {

                val value = iterator.next();
                if(value == null) {
                    continue;
                }

                consumer.accept(value);
            } catch (Throwable e) {
                log.debug("happen ignore Exception", e);
            }
        }
    }

}
