package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

/**
 * <p>
 * Description: CInstantDeserializerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「字符串 / 整型毫秒 / null」多个维度组织，用 InstantBean 验证反序列化。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对字符串与整型毫秒反序列化的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：字符串日期；整型毫秒；null 返回 null。</li>
 *   <li>未覆盖：其他 token 回退默认实现分支（未单列）。</li>
 * </ul>
 * <h2>反序列化</h2>
 * <ul>
 *   <li>1.1 字符串日期：{@code "2025-03-03 08:01:03"} 解析非空（stringInstant）</li>
 *   <li>1.2 整型毫秒：{@code 1731502563000} 转 Instant（numberIntInstant）</li>
 *   <li>1.3 null：返回 null（nullValue）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CInstantDeserializerTests {

    /**
     * 对应测试用例 1.1：字符串日期：{@code "2025-03-03 08:01:03"} 解析非空
     */
    @Test
    public void stringInstant() throws Exception {

        InstantBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"instant\":\"2025-03-03 08:01:03\"}", InstantBean.class);
        Assertions.assertNotNull(bean.getInstant());

    }

    /**
     * 对应测试用例 1.2：整型毫秒：{@code 1731502563000} 转 Instant
     */
    @Test
    public void numberIntInstant() throws Exception {

        InstantBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"instant\":1731502563000}", InstantBean.class);
        Assertions.assertEquals(1731502563000L, bean.getInstant().toEpochMilli());

    }

    /**
     * 对应测试用例 1.3：返回 null
     */
    @Test
    public void nullValue() throws Exception {

        InstantBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"instant\":null}", InstantBean.class);
        Assertions.assertNull(bean.getInstant());

    }

    /**
     * Instant 反序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class InstantBean {

        private Instant instant;

    }

}
