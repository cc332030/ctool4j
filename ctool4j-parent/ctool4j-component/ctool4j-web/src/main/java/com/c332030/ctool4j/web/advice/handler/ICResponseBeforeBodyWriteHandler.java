package com.c332030.ctool4j.web.advice.handler;

import com.c332030.ctool4j.core.function.CFunction;
import com.c332030.ctool4j.spring.interfaces.ICOrdered;

/**
 * <p>
 * Description: ICResponseBeforeBodyWriteHandler
 * </p>
 *
 * @since 2025/9/28
 */
public interface ICResponseBeforeBodyWriteHandler
        extends CFunction<Object, Object>, ICOrdered<ICResponseBeforeBodyWriteHandler> {

}
