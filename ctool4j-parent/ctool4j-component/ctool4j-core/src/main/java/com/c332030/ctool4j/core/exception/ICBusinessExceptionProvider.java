package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.function.CTriFunction;
import com.c332030.ctool4j.core.interfaces.ICError;

/**
 * <p>
 * Description: ICBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public interface ICBusinessExceptionProvider<T extends Throwable> {

    CTriFunction<ICError<?>, String, Throwable, T> getExceptionFunction();

}
