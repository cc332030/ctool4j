package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.function.CRunnable;
import com.c332030.ctool4j.core.function.CSupplier;
import com.c332030.ctool4j.core.util.CSpiUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CExceptionUtils
 * </p>
 *
 * @since 2025/9/14
 */
@CustomLog
@UtilityClass
public class CExceptionUtils {

    public static final ICBusinessExceptionProvider<?> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

    @SuppressWarnings("unchecked")
    public <T extends Throwable> BiFunction<String, Throwable, T> getBusinessExceptionFunction() {
        return (BiFunction<String, Throwable, T>)BUSINESS_EXCEPTION_PROVIDER.getExceptionFunction();
    }

    public <T extends Throwable> T newBusinessException(String message) {
        return newBusinessException(message, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> T newBusinessException(String message, Throwable cause) {
        return (T) getBusinessExceptionFunction().apply(message, cause);
    }

    @SneakyThrows
    public void throwBusinessException(String message) {
        throw newBusinessException(message);
    }

    @SneakyThrows
    public void throwBusinessException(String message, Throwable cause) {
        throw newBusinessException(message, cause);
    }

    public void throwBusinessException(Supplier<String> messageSupplier) {
        throwBusinessException(messageSupplier.get());
    }

    public void throwBusinessException(Supplier<String> messageSupplier, Throwable cause) {
        throwBusinessException(messageSupplier.get(), cause);
    }

    public void ignore(CRunnable runnable) {
        ignore(runnable, "处理失败");
    }

    public void ignore(CRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error(message, e);
        }
    }

    public <T> T ignore(CSupplier<T> supplier) {
        return ignore(supplier, "处理失败");
    }

    public <T> T ignore(CSupplier<T> supplier, String message) {
        try {
            return CSupplier.get(supplier);
        } catch (Exception e) {
            log.error(message, e);
            return null;
        }
    }

}
