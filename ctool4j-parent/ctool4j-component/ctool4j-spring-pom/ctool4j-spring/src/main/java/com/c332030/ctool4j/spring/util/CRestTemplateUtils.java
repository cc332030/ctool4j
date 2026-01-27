package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CHttpClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
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
        CSpringHttpUtils.configureMessageConverters(restTemplate.getMessageConverters(), objectMapper);
        return restTemplate;
    }

}
