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
 * @since 2025/12/12
 */
public class CInstantDeserializerTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void stringInstant() throws Exception {

        InstantBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"instant\":\"2025-03-03 08:01:03\"}", InstantBean.class);
        Assertions.assertNotNull(bean.getInstant());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void numberIntInstant() throws Exception {

        InstantBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"instant\":1731502563000}", InstantBean.class);
        Assertions.assertEquals(1731502563000L, bean.getInstant().toEpochMilli());

    }

    /**
     * 对应测试用例 1.3
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
