package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import com.c332030.ctool4j.spring.util.CRestTemplateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * Description: CRestTemplateUtilsTests
 * </p>
 *
 * <p>覆盖 CRestTemplateUtils.restTemplate，验证创建 RestTemplate 时
 * 替换 Jackson 转换器的 ObjectMapper 并追加 json5 媒体类型</p>
 *
 * @since 2026/8/16
 */
public class CRestTemplateUtilsTests {

    private MappingJackson2HttpMessageConverter findJacksonConverter(RestTemplate restTemplate) {
        return restTemplate.getMessageConverters()
                .stream()
                .filter(messageConverter -> messageConverter instanceof MappingJackson2HttpMessageConverter)
                .map(messageConverter -> (MappingJackson2HttpMessageConverter) messageConverter)
                .findFirst()
                .orElse(null);
    }

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void restTemplate() {
        // 正例：返回的 RestTemplate 非空，Jackson 转换器使用指定 ObjectMapper 并追加 json5
        ObjectMapper objectMapper = new ObjectMapper();

        RestTemplate restTemplate = CRestTemplateUtils.restTemplate(objectMapper);

        Assertions.assertNotNull(restTemplate);

        val jacksonConverter = findJacksonConverter(restTemplate);
        Assertions.assertNotNull(jacksonConverter);
        Assertions.assertSame(objectMapper, jacksonConverter.getObjectMapper());
        Assertions.assertTrue(jacksonConverter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    public void restTemplate_withSharedObjectMapper() {
        // 正例：使用共享 ObjectMapper 时 Jackson 转换器使用同一实例
        RestTemplate restTemplate = CRestTemplateUtils.restTemplate(CJacksonUtils.OBJECT_MAPPER);

        val jacksonConverter = findJacksonConverter(restTemplate);
        Assertions.assertNotNull(jacksonConverter);
        Assertions.assertSame(CJacksonUtils.OBJECT_MAPPER, jacksonConverter.getObjectMapper());
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void restTemplate_nullObjectMapper() {
        // 反例：ObjectMapper 为 null 时，Spring 断言其非空并抛 IllegalArgumentException
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CRestTemplateUtils.restTemplate(null)
        );
    }

}
