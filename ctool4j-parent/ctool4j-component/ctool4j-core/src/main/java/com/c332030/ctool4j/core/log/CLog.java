package com.c332030.ctool4j.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CLog
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>构造：按名称（{@code new CLog("name")}）或按类（{@code new CLog(Class)}）</li>
 *   <li>级别判断：{@code isTraceEnabled} / {@code isDebugEnabled} / {@code isInfoEnabled} / {@code isWarnEnabled} / {@code isErrorEnabled}</li>
 *   <li>打印：trace/debug/info/warn/error 各级别，支持 msg、msg+throwable、msg+args、msg+Supplier 延迟求值</li>
 *   <li>通过传入 {@code org.slf4j.event.Level} 枚举在运行时选择打印级别</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>级别未启用</td>
 *     <td>isXxxEnabled 返回 false，Supplier 参数不求值</td>
 *   </tr>
 *   <tr>
 *     <td>带 throwable</td>
 *     <td>直接打印日志与堆栈</td>
 *   </tr>
 *   <tr>
 *     <td>log 传入 level 为 null</td>
 *     <td>兜底按 error 级别打印</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要统一参数 JSON 化、支持延迟求值的日志场景。</li>
 *   <li>需运行时动态决定打印级别（如按配置/结果枚举映射级别）的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 CLogUtils 的参数转换规则（可 JSON 化类型）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一经 CLogUtils 转换参数，保证日志格式一致；延迟求值提升性能。</li>
 *   <li>动态级别打印 level 参数使用 SLF4J {@code Level} 枚举（不自定义级别集合）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>参数处理</b></p>
 * <ul>
 *   <li>{@code msg, Object... args}：先经 {@code CLogUtils.toLogArgs(args)} 将可 JSON 化参数转为 JSON 字符串，再打印。</li>
 *   <li>{@code msg, Supplier... args}：先判断级别是否启用，启用才求值参数（延迟求值），避免无用参数开销。</li>
 * </ul>
 * <p><b>延迟求值</b></p>
 * <ul>
 *   <li>Supplier 重载在级别未启用时不调用 supplier，减少无谓的参数构造开销。</li>
 * </ul>
 * <p><b>动态级别打印</b></p>
 * <ul>
 *   <li>复用既有参数转换（{@code toLogArgs}）与级别判断逻辑。</li>
 *   <li>级别为 null 时兜底按 error 处理（不静默吞日志）。</li>
 * </ul>
 *
 * @since 2025/3/9
 * @version 1.0
 */
public class CLog {

    private final Logger log;

    /**
     * 按名称构造日志
     *
     * @param name 日志名称
     */
    public CLog(String name) {
        log = LoggerFactory.getLogger(name);
    }

    /**
     * 按类构造日志
     *
     * @param clazz 类
     */
    public CLog(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    /**
     * 是否启用 trace 日志
     *
     * @return 是否启用
     */
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    /**
     * 打印 trace 日志
     *
     * @param msg 日志信息
     */
    public void trace(String msg) {
        log.trace(msg);
    }

    /**
     * 打印 trace 日志
     *
     * @param msg        日志信息
     * @param throwable  异常
     */
    public void trace(String msg, Throwable throwable) {
        log.trace(msg, throwable);
    }

    /**
     * 打印 trace 日志
     *
     * @param msg  日志信息
     * @param args 参数
     */
    public void trace(String msg, Object... args) {
        if(isTraceEnabled()) {
            log.trace(msg, CLogUtils.toLogArgs(args));
        }
    }

    /**
     * 打印 trace 日志（参数延迟求值）
     *
     * @param msg  日志信息
     * @param args 参数供应商
     */
    @SafeVarargs
    public final void trace(String msg, Supplier<Object>... args) {
        if(isTraceEnabled()) {
            trace(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    /**
     * 是否启用 debug 日志
     *
     * @return 是否启用
     */
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /**
     * 打印 debug 日志
     *
     * @param msg 日志信息
     */
    public void debug(String msg) {
        log.debug(msg);
    }

    /**
     * 打印 debug 日志
     *
     * @param msg       日志信息
     * @param throwable 异常
     */
    public void debug(String msg, Throwable throwable) {
        log.debug(msg, throwable);
    }

    /**
     * 打印 debug 日志
     *
     * @param msg  日志信息
     * @param args 参数
     */
    public void debug(String msg, Object... args) {
        if(isDebugEnabled()) {
            log.debug(msg, CLogUtils.toLogArgs(args));
        }
    }

    /**
     * 打印 debug 日志（参数延迟求值）
     *
     * @param msg  日志信息
     * @param args 参数供应商
     */
    @SafeVarargs
    public final void debug(String msg, Supplier<Object> ... args) {
        if(isDebugEnabled()) {
            debug(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    /**
     * 是否启用 info 日志
     *
     * @return 是否启用
     */
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    /**
     * 打印 info 日志
     *
     * @param msg 日志信息
     */
    public void info(String msg) {
        log.info(msg);
    }

    /**
     * 打印 info 日志
     *
     * @param msg       日志信息
     * @param throwable 异常
     */
    public void info(String msg, Throwable throwable) {
        log.info(msg, throwable);
    }

    /**
     * 打印 info 日志（参数延迟求值）
     *
     * @param msg  日志信息
     * @param args 参数供应商
     */
    @SafeVarargs
    public final void info(String msg, Supplier<Object> ... args) {
        if(isInfoEnabled()) {
            info(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    /**
     * 打印 info 日志
     *
     * @param msg  日志信息
     * @param args 参数
     */
    public void info(String msg, Object... args) {
        if(isInfoEnabled()) {
            log.info(msg, CLogUtils.toLogArgs(args));
        }
    }

    /**
     * 是否启用 warn 日志
     *
     * @return 是否启用
     */
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    /**
     * 打印 warn 日志
     *
     * @param msg 日志信息
     */
    public void warn(String msg) {
        log.warn(msg);
    }

    /**
     * 打印 warn 日志
     *
     * @param msg       日志信息
     * @param throwable 异常
     */
    public void warn(String msg, Throwable throwable) {
        log.warn(msg, throwable);
    }

    /**
     * 打印 warn 日志（参数延迟求值）
     *
     * @param msg  日志信息
     * @param args 参数供应商
     */
    @SafeVarargs
    public final void warn(String msg, Supplier<Object> ... args) {
        if(isWarnEnabled()) {
            warn(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    /**
     * 打印 warn 日志
     *
     * @param msg  日志信息
     * @param args 参数
     */
    public void warn(String msg, Object... args) {
        if(isWarnEnabled()) {
            log.warn(msg, CLogUtils.toLogArgs(args));
        }
    }

    /**
     * 是否启用 error 日志
     *
     * @return 是否启用
     */
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    /**
     * 打印 error 日志
     *
     * @param msg 日志信息
     */
    public void error(String msg) {
        log.error(msg);
    }

    /**
     * 打印 error 日志
     *
     * @param msg       日志信息
     * @param throwable 异常
     */
    public void error(String msg, Throwable throwable) {
        log.error(msg, throwable);
    }

    /**
     * 打印 error 日志
     *
     * @param msg  日志信息
     * @param args 参数
     */
    public void error(String msg, Object... args) {
        if(isErrorEnabled()) {
            log.error(msg, CLogUtils.toLogArgs(args));
        }
    }

    /**
     * 打印 error 日志（参数延迟求值）
     *
     * @param msg  日志信息
     * @param args 参数供应商
     */
    @SafeVarargs
    public final void error(String msg, Supplier<Object> ... args) {
        if(isErrorEnabled()) {
            error(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    /**
     * 按指定级别打印日志
     * <ul>
     *   <li>动态级别打印：{@code log(Level, msg)} / {@code log(Level, msg, args)} / {@code log(Level, msg, throwable)}，</li>
     *   <li>{@code log(Level, ...)} 以 {@code org.slf4j.event.Level} 为入参，内部 switch 分发到对应级别的打印方法，</li>
     * </ul>
     *
     * @param level 日志级别
     * @param msg   日志信息
     */
    public void log(Level level, String msg) {
        log(level,
            () -> trace(msg),
            () -> debug(msg),
            () -> info(msg),
            () -> warn(msg),
            () -> error(msg)
        );
    }

    /**
     * 按指定级别打印日志
     *
     * @param level 日志级别
     * @param msg   日志信息
     * @param args  参数
     */
    public void log(Level level, String msg, Object... args) {
        log(level,
            () -> trace(msg, args),
            () -> debug(msg, args),
            () -> info(msg, args),
            () -> warn(msg, args),
            () -> error(msg, args)
        );
    }

    /**
     * 按指定级别打印日志
     *
     * @param level     日志级别
     * @param msg       日志信息
     * @param throwable 异常
     */
    public void log(Level level, String msg, Throwable throwable) {
        log(level,
            () -> trace(msg, throwable),
            () -> debug(msg, throwable),
            () -> info(msg, throwable),
            () -> warn(msg, throwable),
            () -> error(msg, throwable)
        );
    }

    /**
     * 按指定级别选择并执行对应日志动作（级别为 null 时兜底按 error 处理）
     *
     * @param level       日志级别
     * @param traceAction  trace 动作
     * @param debugAction  debug 动作
     * @param infoAction   info 动作
     * @param warnAction   warn 动作
     * @param errorAction  error 动作
     */
    private void log(
        Level level,
        Runnable traceAction,
        Runnable debugAction,
        Runnable infoAction,
        Runnable warnAction,
        Runnable errorAction
    ) {

        if(level == null) {
            errorAction.run();
            return;
        }

        switch (level) {
            case TRACE:
                traceAction.run();
                break;
            case DEBUG:
                debugAction.run();
                break;
            case INFO:
                infoAction.run();
                break;
            case WARN:
                warnAction.run();
                break;
            default:
                errorAction.run();
                break;
        }
    }

}
