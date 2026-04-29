package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
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
@ConditionalOnMissingExceptionHandler(CBusinessException.class)
public class CCBusinessExceptionHandler {

    @ExceptionHandler(CBusinessException.class)
    public CStrResult<Void> handle(CBusinessException e) {

        log.debug("handle CBusinessException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);

        val error = e.getError();
        if(null == error) {
            return CStrResult.error(e.getMessage());
        }

        val msgExtend = e.getMsgExtend();

        return CStrResult.error(
            String.valueOf(error.getCode()),
            CResUtils.formatMessage(error, msgExtend)
        );
    }

}
