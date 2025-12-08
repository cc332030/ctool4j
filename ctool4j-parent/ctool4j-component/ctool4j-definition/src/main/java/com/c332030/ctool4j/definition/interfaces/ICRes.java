package com.c332030.ctool4j.definition.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICRes
 * </p>
 *
 * @since 2025/10/24
 */
public interface ICRes<T> {

    /**
     * 获取响应码
     * @return 响应码
     */
    T getResCode();

    /**
     * 获取响应信息
     * @return 响应信息
     */
    String getResMsg();

    /**
     * 获取日志级别
     * @return 日志级别
     */
    default Level getLogLevel() {
        return Level.ERROR;
    }

}
