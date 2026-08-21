package com.c332030.ctool4j.job.xxljob.task;

import com.c332030.ctool4j.spring.service.ICProxyService;

/**
 * <p>
 * Description: ICTask
 * </p>
 *
 * @see doc/design/xxljob/ICTask.adoc
 * @since 2025/12/26
 */
public interface ICTask<T extends ICTask<T>> extends ICProxyService<T> {

    /**
     * 执行任务（无参数）
     */
    default void execute() {
        currentProxy().execute(null);
    }

    /**
     * 执行任务
     * @param param 任务参数
     */
    default void execute(String param) {
        throw new UnsupportedOperationException();
    }

}
