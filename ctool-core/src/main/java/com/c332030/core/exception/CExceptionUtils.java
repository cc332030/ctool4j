package com.c332030.core.exception;

import com.c332030.core.util.CSpiUtils;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CExceptionUtils
 * </p>
 *
 * @since 2025/9/14
 */
@UtilityClass
public class CExceptionUtils {

    public static final IBusinessExceptionProvider<?> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(IBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

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

}
