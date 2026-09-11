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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「实例非空 / Long 序列化 / Date 格式 / null 处理 / 下划线 / json5」多个维度组织。</li>
 *   <li>用 LongBean/DateBean/NullableBean/SnakeBean 验证各 mapper 行为差异。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各 mapper（Long 转字符串、NON_NULL、snake_case、json5）的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：各 mapper 非空；OBJECT_MAPPER Long 转字符串；Date 统一格式；NON_NULL 忽略 null（LOG 派生同样）；</li>
 *   <li>OBJECT_MAPPER 默认序列化 null；snake_case；json5 无引号/单引号解析。</li>
 *   <li>未覆盖：NATIVE mapper 的数值保留行为、getDefinedModule(numberToString=false) 分支（依赖内部行为，未单列）。</li>
 * </ul>
 * <h2>实例与序列化行为</h2>
 * <ul>
 *   <li>1.1 mappersNotNull：各 mapper 非空（mappersNotNull）</li>
 *   <li>1.2 Long 转字符串：{@code id} 序列化为 {@code "123"}（longToJsonString）</li>
 *   <li>1.3 Date 格式：统一 {@code yyyy-MM-dd HH:mm:ss}（dateToFormattedString）</li>
 *   <li>1.4 null 处理：NON_NULL/LOG 忽略 null、OBJECT_MAPPER 默认序列化 null（nonNullOmitsNull）</li>
 *   <li>1.5 下划线：{@code userName} → {@code user_name}（snakeCase）</li>
 *   <li>1.6 json5：无引号字段名与单引号解析（json5UnquotedAndSingleQuotes）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CJacksonUtilsTests {

    /**
     * 对应测试用例 1.1：各 mapper 非空
     */
    @Test
    public void mappersNotNull() {

        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
        Assertions.assertNotNull(CJacksonUtils.OBJECT_MAPPER_LOG);

    }

    /**
     * 对应测试用例 1.2：Long 转字符串：{@code id} 序列化为 {@code "123"}
     */
    @Test
    public void longToJsonString() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new LongBean(123L));
        Assertions.assertTrue(json.contains("\"id\":\"123\""));

    }

    /**
     * 对应测试用例 1.3：Date 格式：统一 {@code yyyy-MM-dd HH:mm:ss}
     */
    @Test
    public void dateToFormattedString() throws Exception {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new DateBean(date));
        Assertions.assertTrue(json.contains("\"date\":\"2025-03-03 08:01:03\""));

    }

    /**
     * 对应测试用例 1.4：null 处理：NON_NULL/LOG 忽略 null、OBJECT_MAPPER 默认序列化 null
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
     * 对应测试用例 1.5：下划线：{@code userName} → {@code user_name}
     */
    @Test
    public void snakeCase() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE.writeValueAsString(new SnakeBean("hello"));
        Assertions.assertTrue(json.contains("\"user_name\":\"hello\""));

    }

    /**
     * 对应测试用例 1.6：无引号字段名与单引号解析
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
