package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CLogResponseBodyAdvice
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
@CustomLog
@ControllerAdvice
public class CLogResponseBodyAdvice implements ICBaseResponseBodyAdvice<Object> {

    /**
     * 响应体写出前记录响应体到请求日志（仅记录，日志打印由 CRequestLogHandlerInterceptor.afterCompletion 统一执行）
     *
     * @param body                  响应体
     * @param returnType            返回类型
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的转换器类型
     * @param request               请求
     * @param response              响应
     * @return 原响应体
     */
    @Nullable
    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if(BooleanUtil.isTrue(CRequestLogUtils.isEnable())) {
            try {
                // 一次采集响应体 + 响应状态码 + 响应头，供输出响应报文头
                CRequestLogUtils.setRsp(body, null, response);
            } catch (Throwable e) {
                log.error("setRsp failure", e);
            }
        }

        return body;
    }

}
