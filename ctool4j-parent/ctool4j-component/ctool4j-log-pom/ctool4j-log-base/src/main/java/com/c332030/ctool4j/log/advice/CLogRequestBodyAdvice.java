package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.advice.ICBaseRequestBodyAdvice;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Type;

/**
 * <p>
 * Description: CLogRequestBodyAdvice
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
@CustomLog
@ControllerAdvice
public class CLogRequestBodyAdvice implements ICBaseRequestBodyAdvice {

    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {

        if(BooleanUtil.isTrue(CRequestLogUtils.isAdviceEnable())) {
            try {
                CRequestLogUtils.setRequestBodyReq(body);
            } catch (Throwable e) {
                log.error("setReqs failure, url: {}", CRequestUtils.getRequestURIDefaultNull(), e);
            }
        }

        return body;
    }

}
