package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CHttpClientUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

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
        CJsonUtils.configureMessageConverters(restTemplate.getMessageConverters(), objectMapper);

        return restTemplate;
    }

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

}
