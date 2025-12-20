package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.log.util.CTraceUtils;
import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

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

        if(BooleanUtil.isTrue(CRequestLogUtils.isEnable())) {
            try {

                CRequestLogUtils.write(body, null);
                CTraceUtils.removeTraceInfo();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return body;
    }

}
