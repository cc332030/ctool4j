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

    /**
     * 构造业务异常
     *
     * @param error 错误码定义
     */
    public CBusinessException(ICRes<?> error) {
        this(error, (Throwable) null);
    }

    /**
     * 构造业务异常
     *
     * @param error 错误码定义
     * @param cause 异常原因
     */
    public CBusinessException(ICRes<?> error, Throwable cause) {
        this(error, null, cause);
    }

    /**
     * 构造业务异常
     *
     * @param error     错误码定义
     * @param msgExtend 附加信息
     */
    public CBusinessException(ICRes<?> error, String msgExtend) {
        this(error, msgExtend, null);
    }

    /**
     * 构造业务异常
     *
     * @param error     错误码定义
     * @param msgExtend 附加信息
     * @param cause     异常原因
     */
    public CBusinessException(ICRes<?> error, String msgExtend, Throwable cause) {
        super(CResUtils.formatResMessage(error, msgExtend), cause);
        this.error = error;
        this.msgExtend = msgExtend;
    }

}
