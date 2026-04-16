package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CHttpClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * Description: CRestTemplateUtils
 * </p>
 *
 * @since 2025/12/1
 */
@UtilityClass
public class CRestTemplateUtils {

    public final RestTemplate REST_TEMPLATE = restTemplate(CJacksonUtils.OBJECT_MAPPER);

    public RestTemplate restTemplate(ObjectMapper objectMapper) {

        val restTemplate = new RestTemplate(CHttpClientUtils.REQUEST_FACTORY);
        val messageConverters = restTemplate.getMessageConverters();

        messageConverters
            .stream()
            .filter(messageConverter -> messageConverter instanceof MappingJackson2HttpMessageConverter)
            .map(messageConverter -> (MappingJackson2HttpMessageConverter) messageConverter)
            .forEach(messageConverter -> {

                messageConverter.setObjectMapper(objectMapper);
                CSpringHttpUtils.configureJson5(messageConverter);
            });

        return restTemplate;
    }

}
