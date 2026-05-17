package com.c332030.ctool4j.log.interfaces;

import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevelInfo
 * </p>
 *
 * @since 2026/3/20
 */
public interface ICLogLevelInfo extends ICLogLevel {

    @Override
    default Level getLevel() {
        return Level.INFO;
    }

}
