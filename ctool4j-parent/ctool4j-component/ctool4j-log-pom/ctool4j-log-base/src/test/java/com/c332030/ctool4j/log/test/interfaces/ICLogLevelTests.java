package com.c332030.ctool4j.log.test.interfaces;

import com.c332030.ctool4j.log.interfaces.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

/**
 * <p>
 * Description: ICLogLevelTests
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class ICLogLevelTests {

    /**
     * 正常路径：各接口默认 getLevel 返回对应日志级别
     */
    @Test
    void getLevel_trace() {
        ICLogLevelTrace log = new ICLogLevelTrace() {};
        Assertions.assertEquals(Level.TRACE, log.getLevel());
    }

    @Test
    void getLevel_debug() {
        ICLogLevelDebug log = new ICLogLevelDebug() {};
        Assertions.assertEquals(Level.DEBUG, log.getLevel());
    }

    @Test
    void getLevel_info() {
        ICLogLevelInfo log = new ICLogLevelInfo() {};
        Assertions.assertEquals(Level.INFO, log.getLevel());
    }

    @Test
    void getLevel_warn() {
        ICLogLevelWarn log = new ICLogLevelWarn() {};
        Assertions.assertEquals(Level.WARN, log.getLevel());
    }

    @Test
    void getLevel_error() {
        ICLogLevelError log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }

    /**
     * 边界：ICLogLevel 作为公共父接口引用时，getLevel 仍取各实现级别
     */
    @Test
    void getLevel_viaParent() {
        ICLogLevel log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }
}
