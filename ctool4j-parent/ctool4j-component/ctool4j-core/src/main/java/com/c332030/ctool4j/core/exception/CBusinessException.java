package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.interfaces.ICError;
import com.c332030.ctool4j.core.util.CErrorUtils;
import lombok.Getter;

/**
 * <p>
 * Description: CBusinessException
 * </p>
 *
 * @since 2025/9/14
 */
@Getter
public class CBusinessException extends CException {

    private static final long serialVersionUID = 1L;

    private final ICError<?> error;

    private final String errorExtend;

    public CBusinessException(ICError<?> error, Throwable cause) {
        this(error, null, cause);
    }

    public CBusinessException(ICError<?> error, String errorExtend, Throwable cause) {
        super(CErrorUtils.formatMessage(error, errorExtend), cause);
        this.error = error;
        this.errorExtend = errorExtend;
    }

}
