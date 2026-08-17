package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
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
@CAutowiredScan
public class CSpringHttpUtils {

    @Setter
    @CAutowired
    CSpringJacksonConfig jacksonConfig;

    /**
     * 获取 POST 请求头，内容类型与接收类型均为 JSON
     *
     * @return POST 请求头
     */
    public HttpHeaders getPostHeaders() {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 获取 GET 请求头，接收类型为 JSON
     *
     * @return GET 请求头
     */
    public HttpHeaders getGetHeaders() {
        val headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * 配置消息转换器
     * <p>注意：会修改入参集合内各转换器的状态（如为 Jackson 转换器追加 json5 媒体类型），
     * 调用方不应假设入参在调用后保持不变</p>
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
                log.error("configure message converter failed: {}", messageConverter.getClass().getName(), e);
            }

        });
    }

    private final Map<Class<?>, CConsumer<HttpMessageConverter<?>>> MESSAGE_CONVERTER_CONFIGURER = CMap.of(
        MappingJackson2HttpMessageConverter.class,
        e -> configureJackson2HttpMessageConverter((MappingJackson2HttpMessageConverter)e)
    );

    /**
     * 配置 Jackson 消息转换器，开启 json5 时追加支持的媒体类型
     *
     * @param messageConverter Jackson 消息转换器
     */
    public void configureJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter messageConverter) {

        if(CBoolUtils.isTrue(jacksonConfig.getJson5())) {
            configureJson5(messageConverter);
        }

    }

    /**
     * 为消息转换器追加 json5 媒体类型
     *
     * @param messageConverter 消息转换器
     */
    public void configureJson5(AbstractHttpMessageConverter<?> messageConverter) {

        log.debug("enable json5");

        val mediaTypes = CCollUtils.concatOne(
            messageConverter.getSupportedMediaTypes(),
            CMimeTypeEnum.JSON5.getMimeType()
        );
        messageConverter.setSupportedMediaTypes(mediaTypes);

    }

}
