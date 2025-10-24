package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.function.CTriFunction;
import com.c332030.ctool4j.core.interfaces.ICError;

/**
 * <p>
 * Description: CBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public class CBusinessExceptionProvider implements ICBusinessExceptionProvider<CBusinessException> {

    @Override
    public CTriFunction<ICError<?>, String, Throwable, CBusinessException> getExceptionFunction() {
        return CBusinessException::new;
    }

}
