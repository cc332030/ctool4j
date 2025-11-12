package com.c332030.ctool4j.web.advice.handler;

import com.c332030.ctool4j.core.function.CFunction;
import com.c332030.ctool4j.core.function.CTriFunction;
import com.c332030.ctool4j.spring.interfaces.ICOrdered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: ICResponseBeforeBodyWriteHandler
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICResponseBeforeBodyWriteHandler
        extends CFunction<Object, Object>,
        CTriFunction<HttpServletRequest, HttpServletResponse, Object, Object>,
        ICOrdered<ICResponseBeforeBodyWriteHandler>
{

    @Override
    default Object applyThrowable(HttpServletRequest request, HttpServletResponse response, Object o) throws Throwable {
        return apply(o);
    }

    @Override
    default Object applyThrowable(Object o) throws Throwable {
        return o;
    }

}
