package com.c332030.ctool4j.web.controller;

import com.c332030.ctool4j.core.util.CNumUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * <p>
 * Description: CErrorController
 * </p>
 *
 * @since 2026/4/9
 * @see doc/design/web/CErrorController.adoc
 */
@CustomLog
@RestController
@ConditionalOnMissingBean(ErrorController.class)
public class CErrorController implements ErrorController {

    /**
     * 统一错误处理入口，按请求中携带的状态码返回错误结果
     *
     * @param request 请求
     * @return 错误结果
     */
    @RequestMapping("/error")
    public CStrResult<Void> error(HttpServletRequest request) {

        val statusCodeStr = CRequestUtils.getErrorStatusCode(request);
        val exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if(null != exception) {
            log.error("error with code: {}", statusCodeStr, exception);
        }

        // 直接访问 /error 时 ERROR_STATUS_CODE 为空或非法，兜底返回 500
        val httpStatus = Optional.ofNullable(CNumUtils.parseIntDefaultNull(statusCodeStr))
            .map(HttpStatus::resolve)
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return CStrResult.error(httpStatus);
    }

}
