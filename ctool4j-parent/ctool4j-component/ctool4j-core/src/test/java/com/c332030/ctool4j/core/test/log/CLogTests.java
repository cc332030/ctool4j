package com.c332030.ctool4j.core.test.log;

import com.c332030.ctool4j.core.log.CLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLogTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CLogTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void constructByName() {

        CLog log = new CLog("test-logger");
        Assertions.assertNotNull(log);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void constructByClass() {

        CLog log = new CLog(CLogTests.class);
        Assertions.assertNotNull(log);

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void levelEnabled() {

        CLog log = new CLog(CLogTests.class);
        // 仅断言返回 boolean 且调用不抛异常，避免依赖具体日志级别配置
        Assertions.assertNotNull(log.isTraceEnabled());
        Assertions.assertNotNull(log.isDebugEnabled());
        Assertions.assertNotNull(log.isInfoEnabled());
        Assertions.assertNotNull(log.isWarnEnabled());
        Assertions.assertNotNull(log.isErrorEnabled());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void simpleLogs() {

        CLog log = new CLog(CLogTests.class);
        log.trace("trace-msg");
        log.debug("debug-msg");
        log.info("info-msg");
        log.warn("warn-msg");
        log.error("error-msg");

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void throwableLogs() {

        CLog log = new CLog(CLogTests.class);
        Throwable t = new IllegalStateException("boom");
        log.trace("trace", t);
        log.debug("debug", t);
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);

    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void argsLogs() {

        CLog log = new CLog(CLogTests.class);
        log.info("info {} {}", "a", 123);
        log.debug("debug {}", "a");
        log.warn("warn {}", "a");
        log.error("error {}", "a");

    }

    /**
     * 对应测试用例 3.4
     */
    @Test
    public void supplierLogs() {

        CLog log = new CLog(CLogTests.class);
        log.trace("trace {}", () -> "a");
        log.debug("debug {}", () -> "a");
        log.info("info {}", () -> "a");
        log.warn("warn {}", () -> "a");
        log.error("error {}", () -> "a");

    }

}
