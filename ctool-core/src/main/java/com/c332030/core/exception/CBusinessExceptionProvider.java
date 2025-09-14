package com.c332030.core.exception;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public class CBusinessExceptionProvider implements IBusinessExceptionProvider<CBusinessException> {

    @Override
    public BiFunction<String, Throwable, CBusinessException> getExceptionFunction() {
        return CBusinessException::new;
    }

}
