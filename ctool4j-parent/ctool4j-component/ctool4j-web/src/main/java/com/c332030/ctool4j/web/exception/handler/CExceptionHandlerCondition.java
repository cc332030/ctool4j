package com.c332030.ctool4j.web.exception.handler;

import lombok.CustomLog;

/**
 * <p>
 * Description: CExceptionHandlerCondition
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
public class CExceptionHandlerCondition extends CAbstractMissingExceptionHandlerCondition<Exception> {

    public CExceptionHandlerCondition() {
        super(Exception.class);
    }

}
