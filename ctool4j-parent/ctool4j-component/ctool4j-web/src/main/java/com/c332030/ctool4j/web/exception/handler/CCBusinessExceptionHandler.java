package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CCBusinessExceptionHandler
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestControllerAdvice
@Conditional(CCBusinessExceptionHandler.Condition.class)
public class CCBusinessExceptionHandler {

    static class Condition extends CAbstractMissingExceptionHandlerCondition {
        public Condition() {
            super(CBusinessException.class);
        }
    }

    @ExceptionHandler(CBusinessException.class)
    public CStrResult<Void> handle(CBusinessException e) {

        log.debug("handle CBusinessException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val error = e.getError();
        if(null == error) {
            return CStrResult.error(e.getMessage());
        }

        val msgExtend = e.getMsgExtend();

        return CStrResult.error(
            String.valueOf(error.getResCode()),
            CResUtils.formatMessage(error, msgExtend)
        );
    }

}
