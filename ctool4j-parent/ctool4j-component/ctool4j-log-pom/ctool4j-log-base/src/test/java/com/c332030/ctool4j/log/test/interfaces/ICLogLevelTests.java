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
 * 是 {@link ICLogLevel} 及各级别子接口（Trace/Debug/Info/Warn/Error）的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖各级别子接口（Trace/Debug/Info/Warn/Error）default getLevel 返回对应级别。</li>
 *   <li>覆盖以公共父接口 ICLogLevel 引用时仍取实现级别。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各子接口固定级别的约定。</li>
 *   <li>依据黑盒原则与状态/引用形态：每个级别子接口 + 父接口引用均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：Trace/Debug/Info/Warn/Error 五个级别的固定返回、父接口引用返回实现级别。</li>
 *   <li>未覆盖：{@code ICLogLevel} 自身的裸实现（无 default，需实现方指定）。</li>
 * </ul>
 * <h2>各级别接口 getLevel</h2>
 * <ul>
 *   <li>1.1 Trace 返回 TRACE（getLevel_trace）</li>
 *   <li>1.2 Debug 返回 DEBUG（getLevel_debug）</li>
 *   <li>1.3 Info 返回 INFO（getLevel_info）</li>
 *   <li>1.4 Warn 返回 WARN（getLevel_warn）</li>
 *   <li>1.5 Error 返回 ERROR（getLevel_error）</li>
 * </ul>
 * <h2>父接口引用</h2>
 * <ul>
 *   <li>2.1 以 ICLogLevel 引用时仍取实现级别（getLevel_viaParent）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 */
class ICLogLevelTests {

    /**
     * 正常路径：各接口默认 getLevel 返回对应日志级别
     * <p>
     * 对应测试用例 1.1：Trace 返回 TRACE
     */
    @Test
    void getLevel_trace() {
        ICLogLevelTrace log = new ICLogLevelTrace() {};
        Assertions.assertEquals(Level.TRACE, log.getLevel());
    }

    /**
     * Debug 接口返回 DEBUG
     * <p>
     * 对应测试用例 1.2：Debug 返回 DEBUG
     */
    @Test
    void getLevel_debug() {
        ICLogLevelDebug log = new ICLogLevelDebug() {};
        Assertions.assertEquals(Level.DEBUG, log.getLevel());
    }

    /**
     * Info 接口返回 INFO
     * <p>
     * 对应测试用例 1.3：Info 返回 INFO
     */
    @Test
    void getLevel_info() {
        ICLogLevelInfo log = new ICLogLevelInfo() {};
        Assertions.assertEquals(Level.INFO, log.getLevel());
    }

    /**
     * Warn 接口返回 WARN
     * <p>
     * 对应测试用例 1.4：Warn 返回 WARN
     */
    @Test
    void getLevel_warn() {
        ICLogLevelWarn log = new ICLogLevelWarn() {};
        Assertions.assertEquals(Level.WARN, log.getLevel());
    }

    /**
     * Error 接口返回 ERROR
     * <p>
     * 对应测试用例 1.5：Error 返回 ERROR
     */
    @Test
    void getLevel_error() {
        ICLogLevelError log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }

    /**
     * 边界：ICLogLevel 作为公共父接口引用时，getLevel 仍取各实现级别
     * <p>
     * 对应测试用例 2.1：以 ICLogLevel 引用时仍取实现级别
     */
    @Test
    void getLevel_viaParent() {
        ICLogLevel log = new ICLogLevelError() {};
        Assertions.assertEquals(Level.ERROR, log.getLevel());
    }
}
