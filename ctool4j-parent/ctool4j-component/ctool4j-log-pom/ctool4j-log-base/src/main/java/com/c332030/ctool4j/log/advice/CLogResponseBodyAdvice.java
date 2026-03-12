package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
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

        if(BooleanUtil.isTrue(CRequestLogUtils.isAdviceEnable())) {
            try {
                CRequestLogUtils.write(body, null);
            } catch (Throwable e) {
                log.error("write failure", e);
            }
        }

        return body;
    }

}
