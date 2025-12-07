package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.interfaces.ICRes;
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

    private final ICRes<?> error;

    private final String msgExtend;

    public CBusinessException(ICRes<?> error, Throwable cause) {
        this(error, null, cause);
    }

    public CBusinessException(ICRes<?> error, String msgExtend, Throwable cause) {
        super(CResUtils.formatMessage(error, msgExtend), cause);
        this.error = error;
        this.msgExtend = msgExtend;
    }

}
