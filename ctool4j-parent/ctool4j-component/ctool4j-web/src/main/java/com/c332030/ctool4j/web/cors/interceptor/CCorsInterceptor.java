package com.c332030.ctool4j.web.cors.interceptor;

import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import com.c332030.ctool4j.web.interceptor.ICHandlerInterceptor;
import lombok.CustomLog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CCorsInterceptor
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
//@Component
public class CCorsInterceptor implements ICHandlerInterceptor {

    /**
     * 请求前处理：输出 CORS 头，OPTIONS 预检请求返回 false
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {

        CCorsUtils.handle(request, response);
        return !CCorsUtils.handleOptions(request, response);
    }

}
