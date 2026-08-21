package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.advice.ICBaseRequestBodyAdvice;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
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
 * @see doc/design/log/CLogRequestBodyAdvice.adoc
 * @since 2025/12/20
 */
@CustomLog
@ControllerAdvice
public class CLogRequestBodyAdvice implements ICBaseRequestBodyAdvice {

    /**
     * 请求体读取后记录日志
     *
     * @param body          请求体
     * @param inputMessage  输入消息
     * @param parameter     方法参数
     * @param targetType    目标类型
     * @param converterType 消息转换器类型
     * @return 原请求体
     */
    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {

        if(BooleanUtil.isTrue(CRequestLogUtils.isEnable())) {
            try {
                CRequestLogUtils.setRequestBodyReq(body);
            } catch (Throwable e) {
                log.error("setReq failure, url: {}", CRequestUtils.getRequestURIDefaultNull(), e);
            }
        }

        return body;
    }

}
