package com.c332030.ctool4j.web.advice.handler;

import com.c332030.ctool4j.spring.interfaces.ICOrdered;

import java.util.function.Function;

/**
 * <p>
 * Description: ICRequestAfterBodyReadHandler
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICRequestAfterBodyReadHandler
        extends Function<Object, Object>, ICOrdered<ICRequestAfterBodyReadHandler> {

}
