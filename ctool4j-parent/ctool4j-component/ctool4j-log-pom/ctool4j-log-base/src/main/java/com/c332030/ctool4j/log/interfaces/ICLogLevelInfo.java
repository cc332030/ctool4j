package com.c332030.ctool4j.log.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevelInfo
 * </p>
 *
 * @see "doc/design/log/ICLogLevelInfo.adoc"
 * @see "doc/design/log/ICLogLevelTests.adoc"
 * @since 2026/3/20
 */
public interface ICLogLevelInfo extends ICLogLevel {

    /**
     * 获取日志级别
     * @return 日志级别
     */
    @Override
    default Level getLevel() {
        return Level.INFO;
    }

}
