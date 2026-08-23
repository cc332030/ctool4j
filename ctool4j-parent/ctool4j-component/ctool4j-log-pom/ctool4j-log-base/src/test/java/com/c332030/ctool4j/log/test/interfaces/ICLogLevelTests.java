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
 * <p>
 * 是 {@link ICLogLevel} 及各级别子接口（Trace/Debug/Info/Warn/Error）的测试用例（对应测试文档
 * <code>doc/design/log/ICLogLevelTests.adoc</code>）。
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class ICLogLevelTests {

    /**
     * 正常路径：各接口默认 getLevel 返回对应日志级别
     * <p>
     * 对应测试用例 1.1
     */
    @Test
    void getLevel_trace() {
        ICLogLevelTrace log = new ICLogLevelTrace() {};
        Assertions.assertEquals(Level.TRACE, log.getLevel());
    }

    /**
     * Debug 接口返回 DEBUG
     * <p>
     * 对应测试用例 1.2
     */
    @Test
    void getLevel_debug() {
        ICLogLevelDebug log = new ICLogLevelDebug() {};
        Assertions.assertEquals(Level.DEBUG, log.getLevel());
    }

    /**
     * Info 接口返回 INFO
     * <p>
     * 对应测试用例 1.3
     */
    @Test
    void getLevel_info() {
        ICLogLevelInfo log = new ICLogLevelInfo() {};
        Assertions.assertEquals(Level.INFO, log.getLevel());
    }

    /**
     * Warn 接口返回 WARN
     * <p>
     * 对应测试用例 1.4
     */
    @Test
    void getLevel_warn() {
        ICLogLevelWarn log = new ICLogLevelWarn() {};
        Assertions.assertEquals(Level.WARN, log.getLevel());
    }

    /**
     * Error 接口返回 ERROR
     * <p>
     * 对应测试用例 1.5
     */
    @Test
    void getLevel_error() {
        ICLogLevelError log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }

    /**
     * 边界：ICLogLevel 作为公共父接口引用时，getLevel 仍取各实现级别
     * <p>
     * 对应测试用例 2.1
     */
    @Test
    void getLevel_viaParent() {
        ICLogLevel log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }
}
