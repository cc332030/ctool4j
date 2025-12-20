package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
import com.c332030.ctool4j.web.advice.ICBaseRequestBodyAdvice;
import lombok.CustomLog;
import lombok.val;
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

                val argMap = CMap.of(
                        "jsonBody", body
                );
                CRequestLogUtils.setReqs(argMap);
            } catch (Throwable e) {
                log.error("setReqs failure", e);
            }
        }

        return body;
    }

}
