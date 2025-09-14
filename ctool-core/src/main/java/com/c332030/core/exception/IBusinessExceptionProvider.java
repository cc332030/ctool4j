package com.c332030.core.exception;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: IBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public interface IBusinessExceptionProvider<T extends Throwable> {

    BiFunction<String, Throwable, T> getExceptionFunction();

}
