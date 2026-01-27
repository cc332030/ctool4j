package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * <p>
 * Description: CSpringHttpUtils
 * </p>
 *
 * @since 2026/1/27
 */
@UtilityClass
public class CSpringHttpUtils {

    public HttpHeaders getPostHeaders() {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public HttpHeaders getGetHeaders() {
        val headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 配置消息转换器
     * @param messageConverters 消息转换器
     * @param objectMapper 映射器
     */
    public void configureMessageConverters(Collection<HttpMessageConverter<?>> messageConverters, ObjectMapper objectMapper) {
        messageConverters.stream()
            .filter(e -> e instanceof MappingJackson2HttpMessageConverter)
            .forEach(e -> {

                val messageConverter = (MappingJackson2HttpMessageConverter) e;
                if(null != objectMapper) {
                    messageConverter.setObjectMapper(objectMapper);
                }

                val mediaTypes = new LinkedHashSet<>(messageConverter.getSupportedMediaTypes());
                mediaTypes.addAll(CJsonUtils.SUPPORT_MEDIA_TYPES);
                messageConverter.setSupportedMediaTypes(new ArrayList<>(mediaTypes));

            });
    }

}
