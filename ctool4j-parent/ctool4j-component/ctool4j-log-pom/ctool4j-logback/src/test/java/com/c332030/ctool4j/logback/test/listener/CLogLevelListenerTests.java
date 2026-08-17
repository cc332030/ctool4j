package com.c332030.ctool4j.logback.test.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.c332030.ctool4j.logback.listener.CLogLevelListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * <p>
 * Description: CLogLevelListenerTests
 * </p>
 *
 * <p>
 * 覆盖 updateLogLevel 的正常路径（更新生效）、边界（className 为空、level 为空不生效）
 * </p>
 *
 * @since 2026/8/16
 */
class CLogLevelListenerTests {

    private static final String CLASS_NAME = "com.c332030.ctool4j.logback.test.listener.CLogLevelListenerTests";

    /** 本次测试改动的 logger，测试结束后恢复原级别 */
    private Logger affectedLogger;

    private CLogLevelListener newListener(Environment environment) {
        CLogLevelListener listener = new CLogLevelListener(environment);
        return listener;
    }

    @AfterEach
    void restoreLoggerLevel() {
        if (affectedLogger != null) {
            affectedLogger.setLevel(null);
        }
    }

    /**
     * 正常路径：key 合法且 level 有值时，目标 logger 级别更新
     */
    @Test
    void updateLogLevel_normal() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty("logging.level." + CLASS_NAME)).thenReturn("WARN");

        CLogLevelListener listener = newListener(environment);
        listener.updateLogLevel("logging.level." + CLASS_NAME);

        affectedLogger = (Logger) LoggerFactory.getLogger(CLASS_NAME);
        Assertions.assertEquals(Level.WARN, affectedLogger.getLevel());
    }

    /**
     * 正常路径：多次更新可切换级别
     */
    @Test
    void updateLogLevel_switchLevel() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty("logging.level." + CLASS_NAME)).thenReturn("DEBUG");
        CLogLevelListener listener = newListener(environment);
        listener.updateLogLevel("logging.level." + CLASS_NAME);

        Mockito.when(environment.getProperty("logging.level." + CLASS_NAME)).thenReturn("ERROR");
        listener.updateLogLevel("logging.level." + CLASS_NAME);

        affectedLogger = (Logger) LoggerFactory.getLogger(CLASS_NAME);
        Assertions.assertEquals(Level.ERROR, affectedLogger.getLevel());
    }

    /**
     * 边界：key 去除前缀后 className 为空，不生效且不抛异常
     */
    @Test
    void updateLogLevel_blankClassNameNoOp() {
        Environment environment = Mockito.mock(Environment.class);

        CLogLevelListener listener = newListener(environment);
        listener.updateLogLevel("logging.level.");

        // 未触碰 environment.getProperty，也无异常
        Mockito.verify(environment, Mockito.never()).getProperty(Mockito.anyString());
    }

    /**
     * 边界：level 配置值为空，不生效且不抛异常
     */
    @Test
    void updateLogLevel_blankLevelNoOp() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty("logging.level." + CLASS_NAME)).thenReturn("");

        CLogLevelListener listener = newListener(environment);
        listener.updateLogLevel("logging.level." + CLASS_NAME);

        affectedLogger = (Logger) LoggerFactory.getLogger(CLASS_NAME);
        Assertions.assertNull(affectedLogger.getLevel());
    }
}
