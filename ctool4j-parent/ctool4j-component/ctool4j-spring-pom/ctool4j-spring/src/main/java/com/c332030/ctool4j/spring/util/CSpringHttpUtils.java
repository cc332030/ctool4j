package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Description: CSpringHttpUtils
 * </p>
 *
 * @since 2026/1/27
 */
@CustomLog
@UtilityClass
public class CSpringHttpUtils {

    @CAutowired
    CSpringJacksonConfig jacksonConfig;

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

            try {

                val configurer = MESSAGE_CONVERTER_CONFIGURER.get(messageConverter.getClass());
                if (configurer != null) {
                    configurer.accept(messageConverter);
                }
            } catch (Exception e) {
                log.error("", e);
            }

        });
    }

    private final Map<Class<?>, CConsumer<HttpMessageConverter<?>>> MESSAGE_CONVERTER_CONFIGURER = CMap.of(
        MappingJackson2HttpMessageConverter.class,
        e -> configureJackson2HttpMessageConverter((MappingJackson2HttpMessageConverter)e)
    );

    public void configureJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter messageConverter) {

        if(CBoolUtils.isNotTrue(jacksonConfig.getJson5())) {
            return;
        }
        log.info("enable json5");

        val mediaTypes = CCollUtils.concatOne(
            messageConverter.getSupportedMediaTypes(),
            CMimeTypeEnum.JSON5.getMimeType()
        );
        messageConverter.setSupportedMediaTypes(mediaTypes);

    }

}
