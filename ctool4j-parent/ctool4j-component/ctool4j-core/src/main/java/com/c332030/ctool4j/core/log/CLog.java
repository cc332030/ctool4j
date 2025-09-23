package com.c332030.ctool4j.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CLog
 * </p>
 *
 * @since 2025/3/9
 */
public class CLog {

    private final Logger log;

    public CLog(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void trace(String msg) {
        log.trace(msg);
    }

    public void trace(String msg, Throwable throwable) {
        log.trace(msg, throwable);
    }

    public void trace(String msg, Object... args) {
        if(isTraceEnabled()) {
            CLogUtils.dealArgs(args);
            log.trace(msg, args);
        }
    }

    @SafeVarargs
    public final void trace(String msg, Supplier<Object>... args) {
        if(isTraceEnabled()) {
            trace(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void debug(String msg) {
        log.debug(msg);
    }

    public void debug(String msg, Throwable throwable) {
        log.debug(msg, throwable);
    }

    public void debug(String msg, Object... args) {
        if(isDebugEnabled()) {
            CLogUtils.dealArgs(args);
            log.debug(msg, args);
        }
    }

    public void debugNonNull(String msg, Object... args) {
        if(isDebugEnabled()) {
            CLogUtils.dealArgs(args, true);
            log.debug(msg, args);
        }
    }

    @SafeVarargs
    public final void debug(String msg, Supplier<Object> ... args) {
        if(isDebugEnabled()) {
            debug(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public void info(String msg) {
        log.info(msg);
    }

    public void info(String msg, Throwable throwable) {
        log.info(msg, throwable);
    }

    @SafeVarargs
    public final void info(String msg, Supplier<Object> ... args) {
        if(isInfoEnabled()) {
            info(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    public void info(String msg, Object... args) {
        if(isInfoEnabled()) {
            CLogUtils.dealArgs(args);
            log.info(msg, args);
        }
    }

    public void infoNonNull(String msg, Object... args) {
        if(isInfoEnabled()) {
            CLogUtils.dealArgs(args, true);
            log.info(msg, args);
        }
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void warn(String msg) {
        log.warn(msg);
    }

    public void warn(String msg, Throwable throwable) {
        log.warn(msg, throwable);
    }

    @SafeVarargs
    public final void warn(String msg, Supplier<Object> ... args) {
        if(isWarnEnabled()) {
            warn(msg, CLogUtils.getSupplierArgs(args));
        }
    }

    public void warn(String msg, Object... args) {
        if(isWarnEnabled()) {
            CLogUtils.dealArgs(args);
            log.warn(msg, args);
        }
    }

    public void warnNonNull(String msg, Object... args) {
        if(isWarnEnabled()) {
            CLogUtils.dealArgs(args, true);
            log.warn(msg, args);
        }
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public void error(String msg) {
        log.error(msg);
    }

    public void error(String msg, Throwable throwable) {
        log.error(msg, throwable);
    }

    public void error(String msg, Object... args) {
        if(isErrorEnabled()) {
            CLogUtils.dealArgs(args);
            log.error(msg, args);
        }
    }

    public void errorNonNull(String msg, Object... args) {
        if(isErrorEnabled()) {
            CLogUtils.dealArgs(args, true);
            log.error(msg, args);
        }
    }

    @SafeVarargs
    public final void error(String msg, Supplier<Object> ... args) {
        if(isErrorEnabled()) {
            error(msg, CLogUtils.getSupplierArgs(args));
        }
    }

}
