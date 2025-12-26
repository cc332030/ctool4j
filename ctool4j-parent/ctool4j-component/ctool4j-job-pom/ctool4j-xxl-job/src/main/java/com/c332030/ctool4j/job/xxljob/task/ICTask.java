package com.c332030.ctool4j.job.xxljob.task;

import com.c332030.ctool4j.spring.service.ICProxyService;

/**
 * <p>
 * Description: ICTask
 * </p>
 *
 * @since 2025/12/26
 */
public interface ICTask<T extends ICTask<T>> extends ICProxyService<T> {

    default void execute() {
        currentProxy().execute(null);
    }

    default void execute(String param) {
        throw new UnsupportedOperationException();
    }

}
