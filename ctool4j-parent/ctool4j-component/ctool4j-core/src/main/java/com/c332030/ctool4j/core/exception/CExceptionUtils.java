package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.function.CRunnable;
import com.c332030.ctool4j.core.function.CSupplier;
import com.c332030.ctool4j.core.function.CTriFunction;
import com.c332030.ctool4j.core.interfaces.ICError;
import com.c332030.ctool4j.core.util.CSpiUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

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

    @SuppressWarnings("unchecked")
    public static final ICBusinessExceptionProvider<? extends Throwable> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

    public CTriFunction<ICError<?>, String, Throwable, ? extends Throwable> getBusinessExceptionFunction() {
        return BUSINESS_EXCEPTION_PROVIDER.getExceptionFunction();
    }

    public <T extends Throwable> T newBusinessException(ICError<?> error) {
        return newBusinessException(error, null);
    }

    public <T extends Throwable> T newBusinessException(String message) {
        return newBusinessException(null, message);
    }

    public <T extends Throwable> T newBusinessException(ICError<?> error, String message) {
        return newBusinessException(error, message, null);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T newBusinessException(ICError<?> error, String message, Throwable cause) {
        return (T) getBusinessExceptionFunction().apply(error, message, cause);
    }

    @SneakyThrows
    public void throwBusinessException(ICError<?> error) {
        throw newBusinessException(error);
    }

    @SneakyThrows
    public void throwBusinessException(String message) {
        throw newBusinessException(message);
    }

    @SneakyThrows
    public void throwBusinessException(ICError<?> error, String message) {
        throw newBusinessException(error, message);
    }

    @SneakyThrows
    public void throwBusinessException(ICError<?> error, String message, Throwable cause) {
        throw newBusinessException(error, message, cause);
    }

    public void throwBusinessException(Supplier<String> messageSupplier) {
        throwBusinessException(messageSupplier.get());
    }

    public void throwBusinessException(ICError<?> error, Supplier<String> messageSupplier) {
        throwBusinessException(error, messageSupplier.get());
    }

    public void throwBusinessException(ICError<?> error, Supplier<String> messageSupplier, Throwable cause) {
        throwBusinessException(error, messageSupplier.get(), cause);
    }

    public void ignore(CRunnable runnable) {
        ignore(runnable, "处理失败");
    }

    public void ignore(CRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable e) {
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
