package com.c332030.ctool4j.core.test.log;

import com.c332030.ctool4j.core.log.CLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * <p>
 * Description: CLogTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 级别判断 / 简单打印 / 异常打印 / 参数打印 / 延迟求值」多个维度组织。</li>
 *   <li>构造覆盖按名称与按类；级别判断校验 CLog 各 isEnabled 与底层 SLF4J Logger 委托一致（不依赖具体级别配置）。</li>
 *   <li>打印覆盖各级别，参数与 Supplier 两种形态。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各构造、级别判断与打印形态的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：构造两入口、级别判断、打印各形态。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：按名称/按类构造；五级别 isEnabled；五级别简单打印；带 throwable 打印；Object 参数打印；</li>
 *   <li>Supplier 延迟求值打印。</li>
 *   <li>未覆盖：CLogUtils 参数 JSON 化的具体内容（依赖日志 mapper，未在单测断言）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 按名称：{@code new CLog("test-logger")} 非空（constructByName）</li>
 *   <li>1.2 按类：{@code new CLog(CLogTests.class)} 非空（constructByClass）</li>
 * </ul>
 * <h2>级别判断</h2>
 * <ul>
 *   <li>2.1 五级别 isEnabled：CLog 各级别判断与底层 SLF4J Logger 委托一致（levelEnabled）</li>
 * </ul>
 * <h2>打印</h2>
 * <ul>
 *   <li>3.1 简单打印：五级别 msg 打印（simpleLogs）</li>
 *   <li>3.2 带异常打印：五级别 msg+throwable（throwableLogs）</li>
 *   <li>3.3 Object 参数打印：五级别 msg+args（argsLogs）</li>
 *   <li>3.4 Supplier 延迟求值：五级别 msg+Supplier（supplierLogs）</li>
 * </ul>
 * <h2>动态级别打印</h2>
 * <ul>
 *   <li>4.1 简单打印：五级别 log(Level, msg) 打印（levelSimpleLogs）</li>
 *   <li>4.2 Object 参数打印：五级别 log(Level, msg, args) 打印（levelArgsLogs）</li>
 *   <li>4.3 带异常打印：五级别 log(Level, msg, throwable) 打印（levelThrowableLogs）</li>
 *   <li>4.4 null 级别兜底：log(null, msg) 按 error 处理不抛异常（levelNullFallback）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CLogTests {

    /**
     * 对应测试用例 1.1：按名称：{@code new CLog("test-logger")} 非空
     */
    @Test
    public void constructByName() {

        CLog log = new CLog("test-logger");
        Assertions.assertNotNull(log);

    }

    /**
     * 对应测试用例 1.2：按类：{@code new CLog(CLogTests.class)} 非空
     */
    @Test
    public void constructByClass() {

        CLog log = new CLog(CLogTests.class);
        Assertions.assertNotNull(log);

    }

    /**
     * 对应测试用例 2.1：五级别 isEnabled：CLog 各级别判断与底层 SLF4J Logger 委托一致
     */
    @Test
    public void levelEnabled() {

        CLog log = new CLog(CLogTests.class);
        // 校验 CLog 各级别判断与底层 SLF4J Logger 委托一致（不依赖具体日志级别配置）
        Logger logger = LoggerFactory.getLogger(CLogTests.class);
        Assertions.assertEquals(logger.isTraceEnabled(), log.isTraceEnabled());
        Assertions.assertEquals(logger.isDebugEnabled(), log.isDebugEnabled());
        Assertions.assertEquals(logger.isInfoEnabled(), log.isInfoEnabled());
        Assertions.assertEquals(logger.isWarnEnabled(), log.isWarnEnabled());
        Assertions.assertEquals(logger.isErrorEnabled(), log.isErrorEnabled());

    }

    /**
     * 对应测试用例 3.1：简单打印：五级别 msg 打印
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
     * 对应测试用例 3.2：带异常打印：五级别 msg+throwable
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
     * 对应测试用例 3.3：Object 参数打印：五级别 msg+args
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
     * 对应测试用例 3.4：Supplier 延迟求值：五级别 msg+Supplier
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

    /**
     * 对应测试用例 4.1：简单打印：五级别 log(Level, msg) 打印
     */
    @Test
    public void levelSimpleLogs() {

        CLog log = new CLog(CLogTests.class);
        log.log(Level.TRACE, "trace-msg");
        log.log(Level.DEBUG, "debug-msg");
        log.log(Level.INFO, "info-msg");
        log.log(Level.WARN, "warn-msg");
        log.log(Level.ERROR, "error-msg");

    }

    /**
     * 对应测试用例 4.2：Object 参数打印：五级别 log(Level, msg, args) 打印
     */
    @Test
    public void levelArgsLogs() {

        CLog log = new CLog(CLogTests.class);
        log.log(Level.TRACE, "trace {}", "a");
        log.log(Level.DEBUG, "debug {}", "a");
        log.log(Level.INFO, "info {} {}", "a", 123);
        log.log(Level.WARN, "warn {}", "a");
        log.log(Level.ERROR, "error {}", "a");

    }

    /**
     * 对应测试用例 4.3：带异常打印：五级别 log(Level, msg, throwable) 打印
     */
    @Test
    public void levelThrowableLogs() {

        CLog log = new CLog(CLogTests.class);
        Throwable t = new IllegalStateException("boom");
        log.log(Level.TRACE, "trace", t);
        log.log(Level.DEBUG, "debug", t);
        log.log(Level.INFO, "info", t);
        log.log(Level.WARN, "warn", t);
        log.log(Level.ERROR, "error", t);

    }

    /**
     * 对应测试用例 4.4：null 级别兜底：log(null, msg) 按 error 处理不抛异常
     */
    @Test
    public void levelNullFallback() {

        CLog log = new CLog(CLogTests.class);
        // null 级别兜底按 error 处理，不抛异常
        log.log(null, "null-level-msg");

    }

}
