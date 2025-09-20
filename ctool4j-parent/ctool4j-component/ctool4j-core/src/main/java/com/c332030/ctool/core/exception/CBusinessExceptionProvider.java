package com.c332030.ctool.core.exception;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public class CBusinessExceptionProvider implements ICBusinessExceptionProvider<CBusinessException> {

    @Override
    public BiFunction<String, Throwable, CBusinessException> getExceptionFunction() {
        return CBusinessException::new;
    }

}
