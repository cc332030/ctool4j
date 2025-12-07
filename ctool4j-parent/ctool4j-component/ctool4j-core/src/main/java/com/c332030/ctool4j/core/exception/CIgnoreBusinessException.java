package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.definition.interfaces.ICError;

/**
 * <p>
 * Description: CIgnoreBusinessException
 * </p>
 *
 * @author c332030
 * @since 2025/12/7
 */
public class CIgnoreBusinessException extends CBusinessException {

    private static final long serialVersionUID = 1L;

    public CIgnoreBusinessException(ICError<?> error, Throwable cause) {
        super(error, cause);
    }

    public CIgnoreBusinessException(ICError<?> error, String errorExtend, Throwable cause) {
        super(error, errorExtend, cause);
    }
}
