package com.c332030.ctool4j.log.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevelWarn
 * </p>
 *
 * @since 2026/3/20
 */
public interface ICLogLevelWarn extends ICLogLevel {

    /**
     * 获取日志级别
     * @return 日志级别
     */
    @Override
    default Level getLevel() {
        return Level.WARN;
    }

}
