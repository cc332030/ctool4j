package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
     *
     * @param messageConverters 消息转换器
     */
    public void configureMessageConverters(Collection<HttpMessageConverter<?>> messageConverters) {
        messageConverters.forEach(messageConverter -> {

            if(messageConverter instanceof AbstractHttpMessageConverter) {
                configureAbstractHttpMessageConverter((AbstractHttpMessageConverter<?>) messageConverter);
            }

            val configurer = MESSAGE_CONVERTER_CONFIGURER.get(messageConverter.getClass());
            if (configurer != null) {
                configurer.accept(messageConverter);
            }

        });
    }

    private final Map<Class<?>, CConsumer<HttpMessageConverter<?>>> MESSAGE_CONVERTER_CONFIGURER = CMap.of(
        MappingJackson2HttpMessageConverter.class,
        e -> configureJackson2HttpMessageConverter((MappingJackson2HttpMessageConverter)e)
    );

    public void configureAbstractHttpMessageConverter(AbstractHttpMessageConverter<?> messageConverter) {

        messageConverter.setDefaultCharset(StandardCharsets.UTF_8);

    }

    public void configureJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter messageConverter) {

        val mediaTypes = new LinkedHashSet<>(messageConverter.getSupportedMediaTypes());
        mediaTypes.addAll(CJsonUtils.SUPPORT_MEDIA_TYPES);
        messageConverter.setSupportedMediaTypes(new ArrayList<>(mediaTypes));

    }

}
