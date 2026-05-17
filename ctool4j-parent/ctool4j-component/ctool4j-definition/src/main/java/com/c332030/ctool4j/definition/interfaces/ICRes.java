package com.c332030.ctool4j.definition.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICRes
 * </p>
 *
 * @since 2025/10/24
 */
public interface ICRes<T> extends ICCode<T>, ICMsg {

    /**
     * 获取日志级别
     * @return 日志级别
     */
    default Level getLogLevel() {
        return Level.ERROR;
    }

}
