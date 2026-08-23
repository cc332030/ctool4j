package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.LinkedHashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CExceptionUtils
 * </p>
 *
 * @since 2025/9/14
 * @see "doc/design/core/CExceptionUtils.adoc"
 * @see "doc/design/core/CExceptionUtilsTests.adoc"
 */
@CustomLog
@UtilityClass
public class CExceptionUtils {

    /**
     * 业务异常提供者（优先取自定义 SPI 实现）
     */
    @SuppressWarnings("unchecked")
    public static final ICBusinessExceptionProvider<? extends Throwable> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

    /**
     * 获取业务异常构造函数
     *
     * @return 业务异常构造函数（错误码、附加信息、原因构造异常）
     */
    public CTriFunction<ICRes<?>, String, Throwable, ? extends Throwable> getBusinessExceptionFunction() {
        return BUSINESS_EXCEPTION_PROVIDER.getExceptionFunction();
    }

    /**
     * 根据错误码构造业务异常
     *
     * @param error 错误码定义
     * @param <T>   异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(ICRes<?> error) {
        return newBusinessException(error, null);
    }

    /**
     * 根据信息构造业务异常
     *
     * @param message 异常信息
     * @param <T>     异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(String message) {
        return newBusinessException(null, message);
    }

    /**
     * 根据错误码和信息构造业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param <T>     异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(ICRes<?> error, String message) {
        return newBusinessException(error, message, null);
    }

    /**
     * 根据错误码、信息和原因构造业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param cause   异常原因
     * @param <T>     异常类型
     * @return 业务异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T newBusinessException(ICRes<?> error, String message, Throwable cause) {
        return (T) getBusinessExceptionFunction().apply(error, message, cause);
    }

    /**
     * 抛出业务异常
     *
     * @param error 错误码定义
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error) {
        throw newBusinessException(error);
    }

    /**
     * 抛出业务异常
     *
     * @param message 异常信息
     */
    @SneakyThrows
    public void throwBusinessException(String message) {
        throw newBusinessException(message);
    }

    /**
     * 抛出业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error, String message) {
        throw newBusinessException(error, message);
    }

    /**
     * 抛出业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param cause   异常原因
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error, String message, Throwable cause) {
        throw newBusinessException(error, message, cause);
    }

    /**
     * 抛出业务异常
     *
     * @param messageSupplier 异常信息供应商
     */
    public void throwBusinessException(Supplier<String> messageSupplier) {
        throwBusinessException(messageSupplier.get());
    }

    /**
     * 抛出业务异常
     *
     * @param error           错误码定义
     * @param messageSupplier 异常信息供应商
     */
    public void throwBusinessException(ICRes<?> error, Supplier<String> messageSupplier) {
        throwBusinessException(error, messageSupplier.get());
    }

    /**
     * 抛出业务异常
     *
     * @param error           错误码定义
     * @param messageSupplier 异常信息供应商
     * @param cause           异常原因
     */
    public void throwBusinessException(ICRes<?> error, Supplier<String> messageSupplier, Throwable cause) {
        throwBusinessException(error, messageSupplier.get(), cause);
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param runnable 待执行操作
     */
    public void ignore(CRunnable runnable) {
        ignore(runnable, "处理失败");
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param runnable 待执行操作
     * @param message  失败日志信息
     */
    public void ignore(CRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error(message, e);
        }
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param supplier 结果供应商
     * @param <T>      结果类型
     * @return 执行结果，失败时返回 null
     */
    public <T> T ignore(CSupplier<T> supplier) {
        return ignore(supplier, "处理失败");
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param supplier 结果供应商
     * @param message  失败日志信息
     * @param <T>      结果类型
     * @return 执行结果，失败时返回 null
     */
    public <T> T ignore(CSupplier<T> supplier, String message) {
        try {
            return CSupplier.get(supplier);
        } catch (Throwable e) {
            log.error(message, e);
            return null;
        }
    }

    /**
     * 获取异常信息及其所有 cause 信息
     *
     * @param throwable 异常
     * @return 异常链信息；异常为 null 时返回 null
     */
    public String getMessageWithCause(Throwable throwable) {

        if(null == throwable) {
            return null;
        }

        val throwableSet = new LinkedHashSet<Throwable>();

        var throwableNew = throwable;
        while (true) {

            throwableSet.add(throwableNew);
            val cause = throwableNew.getCause();
            if(null == cause || throwableSet.contains(cause)) {
                break;
            }
            throwableNew = cause;

        }

        return throwableSet.stream()
            .map(Throwable::getMessage)
            .collect(Collectors.joining("\ncause by "));
    }

}
