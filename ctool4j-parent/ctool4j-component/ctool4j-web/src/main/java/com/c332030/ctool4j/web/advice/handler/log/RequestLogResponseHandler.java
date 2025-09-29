package com.c332030.ctool4j.web.advice.handler.log;

import com.c332030.ctool4j.web.advice.handler.ICResponseBeforeBodyWriteHandler;

/**
 * <p>
 * Description: RequestLogResponseHandler
 * </p>
 *
 * @since 2025/9/28
 */
public class RequestLogResponseHandler implements ICResponseBeforeBodyWriteHandler {

    @Override
    public Object apply(Object o) throws Throwable {
        return o;
    }

}
