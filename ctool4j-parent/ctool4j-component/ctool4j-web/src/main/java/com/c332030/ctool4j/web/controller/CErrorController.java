package com.c332030.ctool4j.web.controller;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.CustomLog;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CErrorController
 * </p>
 *
 * @since 2026/4/9
 */
@CustomLog
@RestController
@ConditionalOnMissingBean(ErrorController.class)
public class CErrorController implements ErrorController {

    @RequestMapping("/error")
    public CStrResult<Void> error(HttpServletRequest request) {

        val statusCodeStr = (String) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        val exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if(null != exception) {
            log.error("error with code: {}", statusCodeStr, exception);
        }

        val httpStatus = HttpStatus.valueOf(Integer.parseInt(statusCodeStr));
        return CStrResult.error(httpStatus);
    }

}
