package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CJacksonUtilsTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CJacksonUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void mappersNotNull() {

        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_LOG);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void longToJsonString() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new LongBean(123L));
        Assertions.assertTrue(json.contains("\"id\":\"123\""));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void dateToFormattedString() throws Exception {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new DateBean(date));
        Assertions.assertTrue(json.contains("\"date\":\"2025-03-03 08:01:03\""));

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void nonNullOmitsNull() throws Exception {

        // OBJECT_MAPPER_NON_NULL：忽略 null 字段
        String json = CJacksonUtils.OBJECT_MAPPER_NON_NULL.writeValueAsString(new NullableBean("a", null));
        Assertions.assertFalse(json.contains("\"b\""));

        // OBJECT_MAPPER_LOG 从 NON_NULL 派生，同样忽略 null 字段
        String logJson = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new NullableBean("a", null));
        Assertions.assertFalse(logJson.contains("\"b\""));

        // OBJECT_MAPPER：默认序列化 null 字段
        String normalJson = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new NullableBean("a", null));
        Assertions.assertTrue(normalJson.contains("\"b\":null"));

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void snakeCase() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE.writeValueAsString(new SnakeBean("hello"));
        Assertions.assertTrue(json.contains("\"user_name\":\"hello\""));

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void json5UnquotedAndSingleQuotes() throws Exception {

        JsonNode node = CJacksonUtils.OBJECT_MAPPER.readTree("{name: 'tom'}");
        Assertions.assertEquals("tom", node.get("name").asText());

    }

    /**
     * Long 序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class LongBean {

        private Long id;

    }

    /**
     * Date 序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class DateBean {

        private Date date;

    }

    /**
     * null 序列化测试 Bean
     */
    @Getter
    @RequiredArgsConstructor
    static class NullableBean {

        private final String a;
        private final String b;

    }

    /**
     * 下划线序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SnakeBean {

        private String userName;

    }

}
