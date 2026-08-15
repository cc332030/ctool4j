package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import com.c332030.ctool4j.spring.util.CSpringHttpUtils;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CSpringHttpUtilsTests
 * </p>
 *
 * <p>覆盖 CSpringHttpUtils 的请求头构建与消息转换器配置逻辑，
 * 通过静态 setter 注入 CSpringJacksonConfig</p>
 *
 * @since 2026/8/16
 */
public class CSpringHttpUtilsTests {

    private CSpringJacksonConfig jacksonConfig;

    @BeforeEach
    public void setUp() {
        jacksonConfig = new CSpringJacksonConfig();
        CSpringHttpUtils.setJacksonConfig(jacksonConfig);
    }

    @AfterEach
    public void tearDown() {
        // 还原静态 jacksonConfig，避免污染其他用例
        CSpringHttpUtils.setJacksonConfig(null);
    }

    // ---------- getPostHeaders ----------

    @Test
    public void getPostHeaders() {
        // 正例：内容类型与接收类型均为 JSON
        val headers = CSpringHttpUtils.getPostHeaders();

        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        Assertions.assertEquals(Collections.singletonList(MediaType.APPLICATION_JSON),
                headers.getAccept());
    }

    // ---------- getGetHeaders ----------

    @Test
    public void getGetHeaders() {
        // 正例：接收类型为 JSON，无内容类型
        val headers = CSpringHttpUtils.getGetHeaders();

        Assertions.assertNull(headers.getContentType());
        Assertions.assertEquals(Collections.singletonList(MediaType.APPLICATION_JSON),
                headers.getAccept());
    }

    // ---------- configureMessageConverters ----------

    @Test
    public void configureMessageConverters_whenJacksonAndJson5() {
        // 正例：包含 Jackson 转换器且开启 json5 时，追加 json5 媒体类型
        jacksonConfig.setJson5(Boolean.TRUE);
        val jacksonConverter = new MappingJackson2HttpMessageConverter();

        List<HttpMessageConverter<?>> converters =
                Collections.singletonList(jacksonConverter);
        CSpringHttpUtils.configureMessageConverters(converters);

        Assertions.assertTrue(jacksonConverter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

    @Test
    public void configureMessageConverters_whenJacksonAndNotJson5() {
        // 反例：开启 json5 为 false 时不追加 json5 媒体类型
        jacksonConfig.setJson5(Boolean.FALSE);
        val jacksonConverter = new MappingJackson2HttpMessageConverter();

        List<HttpMessageConverter<?>> converters =
                Collections.singletonList(jacksonConverter);
        CSpringHttpUtils.configureMessageConverters(converters);

        Assertions.assertFalse(jacksonConverter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

    @Test
    public void configureMessageConverters_whenUnknownConverter() {
        // 反例：未知类型转换器时无配置逻辑且不抛异常
        jacksonConfig.setJson5(Boolean.TRUE);
        val otherConverter = new StringHttpMessageConverter();

        List<HttpMessageConverter<?>> converters =
                Collections.singletonList(otherConverter);
        CSpringHttpUtils.configureMessageConverters(converters);

        Assertions.assertFalse(otherConverter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

    // ---------- configureJackson2HttpMessageConverter ----------

    @Test
    public void configureJackson2HttpMessageConverter_whenJson5() {
        // 正例：开启 json5 时追加 json5 媒体类型
        jacksonConfig.setJson5(Boolean.TRUE);
        val converter = new MappingJackson2HttpMessageConverter();

        CSpringHttpUtils.configureJackson2HttpMessageConverter(converter);

        Assertions.assertTrue(converter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

    @Test
    public void configureJackson2HttpMessageConverter_whenNotJson5() {
        // 反例：未开启 json5 时不追加 json5 媒体类型
        jacksonConfig.setJson5(Boolean.FALSE);
        val converter = new MappingJackson2HttpMessageConverter();

        CSpringHttpUtils.configureJackson2HttpMessageConverter(converter);

        Assertions.assertFalse(converter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

    // ---------- configureJson5 ----------

    @Test
    public void configureJson5() {
        // 正例：直接追加 json5 媒体类型
        val converter = new MappingJackson2HttpMessageConverter();

        CSpringHttpUtils.configureJson5(converter);

        Assertions.assertTrue(converter.getSupportedMediaTypes()
                .contains(CMimeTypeEnum.JSON5.getMimeType()));
    }

}
