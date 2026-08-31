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
 * @since 2025/3/9
 * @see "doc/design/core/CLog.adoc"
 * @see "doc/design/core/CLogTests.adoc"
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
