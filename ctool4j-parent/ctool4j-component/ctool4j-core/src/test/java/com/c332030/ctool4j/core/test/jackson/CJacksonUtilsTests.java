package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.fasterxml.jackson.databind.JsonNode;
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

    @Test
    public void mappersNotNull() {

        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);

    }

    @Test
    public void longToJsonString() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new LongBean(123L));
        Assertions.assertTrue(json.contains("\"id\":\"123\""));

    }

    @Test
    public void dateToFormattedString() throws Exception {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new DateBean(date));
        Assertions.assertTrue(json.contains("\"date\":\"2025-03-03 08:01:03\""));

    }

    @Test
    public void nonNullOmitsNull() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_NON_NULL.writeValueAsString(new NullableBean("a", null));
        Assertions.assertFalse(json.contains("\"b\""));

        String normalJson = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new NullableBean("a", null));
        Assertions.assertTrue(normalJson.contains("\"b\":null"));

    }

    @Test
    public void snakeCase() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE.writeValueAsString(new SnakeBean("hello"));
        Assertions.assertTrue(json.contains("\"user_name\":\"hello\""));

    }

    @Test
    public void json5UnquotedAndSingleQuotes() throws Exception {

        JsonNode node = CJacksonUtils.OBJECT_MAPPER.readTree("{name: 'tom'}");
        Assertions.assertEquals("tom", node.get("name").asText());

    }

    /**
     * Long 序列化测试 Bean
     */
    static class LongBean {

        private Long id;

        LongBean(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    /**
     * Date 序列化测试 Bean
     */
    static class DateBean {

        private Date date;

        DateBean(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

    }

    /**
     * null 序列化测试 Bean
     */
    static class NullableBean {

        private final String a;
        private final String b;

        NullableBean(String a, String b) {
            this.a = a;
            this.b = b;
        }

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }

    }

    /**
     * 下划线序列化测试 Bean
     */
    static class SnakeBean {

        private String userName;

        SnakeBean(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

    }

}
