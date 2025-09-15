package com.c332030.ctool.core.exception;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: ICBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public interface ICBusinessExceptionProvider<T extends Throwable> {

    BiFunction<String, Throwable, T> getExceptionFunction();

}
