package com.c332030.ctool4j.web.advice.handler;

import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.spring.interfaces.ICOrdered;

/**
 * <p>
 * Description: ICRequestAfterBodyReadHandler
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICRequestAfterBodyReadHandler
        extends CFunction<Object, Object>, ICOrdered<ICRequestAfterBodyReadHandler> {

}
