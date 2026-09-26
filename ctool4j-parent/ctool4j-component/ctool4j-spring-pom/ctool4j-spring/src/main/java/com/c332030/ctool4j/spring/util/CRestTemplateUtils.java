package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CHttpClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

/**
 * <p>
 * Description: CRestTemplateUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRestTemplateUtils}：RestTemplate 工具。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>RestTemplate 相关处理</li>
 *   <li>Jackson 转换器装配：把入参 {@code ObjectMapper}（Jackson 2）写入 RestTemplate 的 Jackson 2 转换器；
 *   默认转换器缺失时（Spring Framework 7 起默认注册的是 Jackson 3 转换器）主动补一个，避免入参被静默忽略。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>HTTP 请求</p>
 * <h2>不适用与边界场景</h2>
 * <p>静态工具</p>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>只装配 <b>Jackson 2</b> 转换器（{@code MappingJackson2HttpMessageConverter}）：Jackson 2 / 3 的类型体系不兼容
 *   （{@code com.fasterxml.jackson.databind.ObjectMapper} ↔ {@code tools.jackson.databind.JsonMapper}），
 *   本工具不承载 Jackson 3 的转换器装配。</li>
 * </ul>
 *
 * @since 2025/12/1
 * @version 1.0
 */
@UtilityClass
public class CRestTemplateUtils {

    /**
     * 使用默认 ObjectMapper 创建的共享 RestTemplate
     */
    public final RestTemplate REST_TEMPLATE = restTemplate(CJacksonUtils.OBJECT_MAPPER);

    /**
     * 创建 RestTemplate 并替换 Jackson 转换器的 ObjectMapper
     *
     * @param objectMapper ObjectMapper
     * @return 配置完成的 RestTemplate
     */
    public RestTemplate restTemplate(ObjectMapper objectMapper) {

        val restTemplate = new RestTemplate(CHttpClientUtils.REQUEST_FACTORY);
        val messageConverters = restTemplate.getMessageConverters();

        val jackson2Converters = messageConverters
            .stream()
            .filter(messageConverter -> messageConverter instanceof MappingJackson2HttpMessageConverter)
            .map(messageConverter -> (MappingJackson2HttpMessageConverter) messageConverter)
            .collect(Collectors.toList());

        // 默认转换器里没有 Jackson 2 转换器时主动补一个（置于容器末尾，不改变既有转换器的优先级）。
        // Spring Framework 7 起 RestTemplate 默认注册的是 Jackson 3 的 JacksonJsonHttpMessageConverter，
        // 而本工具面向 Jackson 2（{@link CJacksonUtils}）——只"遍历已有转换器改 ObjectMapper"会一个都改不到，
        // 表现为"入参 ObjectMapper 被静默忽略"，故此处补齐而非静默失效。
        if (jackson2Converters.isEmpty()) {
            val converter = new MappingJackson2HttpMessageConverter();
            converter.setObjectMapper(objectMapper);
            messageConverters.add(converter);
            jackson2Converters.add(converter);
        }

        jackson2Converters.forEach(messageConverter -> {

            messageConverter.setObjectMapper(objectMapper);
            CSpringHttpUtils.configureJson5(messageConverter);
        });

        return restTemplate;
    }

}
