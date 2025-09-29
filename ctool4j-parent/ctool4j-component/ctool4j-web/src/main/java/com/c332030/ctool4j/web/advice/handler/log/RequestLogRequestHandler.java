package com.c332030.ctool4j.web.advice.handler.log;

import com.c332030.ctool4j.web.advice.handler.ICRequestAfterBodyReadHandler;

/**
 * <p>
 * Description: RequestLogRequestHandler
 * </p>
 *
 * @since 2025/9/28
 */
public class RequestLogRequestHandler implements ICRequestAfterBodyReadHandler {

    @Override
    public Object apply(Object o) throws Throwable {
        return o;
    }

}
