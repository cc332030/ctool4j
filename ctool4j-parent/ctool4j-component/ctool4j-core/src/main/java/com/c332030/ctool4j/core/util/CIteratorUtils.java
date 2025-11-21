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
 */
@CustomLog
@UtilityClass
public class CIteratorUtils {

    public <T> void forEachIgnoreException(Iterable<T> iterable, CConsumer<T> consumer) {

        if(iterable == null) {
            return;
        }

        forEachIgnoreException(iterable.iterator(), consumer);

    }

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
