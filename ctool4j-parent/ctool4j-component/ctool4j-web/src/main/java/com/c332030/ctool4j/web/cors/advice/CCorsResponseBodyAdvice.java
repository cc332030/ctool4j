package com.c332030.ctool4j.web.cors.advice;

import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import com.c332030.ctool4j.web.cors.util.CCorsUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CCorsResponseBodyAdvice
 * </p>
 *
 * <p>
 * CORS 备用方案：Filter 已自动生效时无需注册本 Advice；
 * 需要时由使用方手动注册
 * </p>
 *
 * @since 2025/11/12
 * @see "doc/design/web/CCorsResponseBodyAdvice.adoc"
 */
@CustomLog
//@ControllerAdvice
@AllArgsConstructor
public class CCorsResponseBodyAdvice implements ICBaseResponseBodyAdvice<Object> {

    /**
     * 响应写入前处理 CORS 头
     *
     * @param body                   响应体
     * @param returnType             返回类型
     * @param selectedContentType    选中的内容类型
     * @param selectedConverterType  选中的转换器类型
     * @param request                请求
     * @param response               响应
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
        CCorsUtils.handle(request, response);
        return body;
    }

}
