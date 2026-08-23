package com.c332030.ctool4j.redis.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CValueWithTtlTests
 * </p>
 *
 * <p>
 * 是 {@link CValueWithTtl} 的测试用例（对应测试文档
 * <code>doc/design/redis/CValueWithTtlTests.adoc</code>）。
 * </p>
 */
public class CValueWithTtlTests {

    /** 对应测试用例 1.1 */
    @Test
    void allArgsConstructor_setsFields() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>("value", 100L);

        Assertions.assertEquals("value", valueWithTtl.getValue());
        Assertions.assertEquals(100L, valueWithTtl.getTtl());
    }

    /** 对应测试用例 1.2 */
    @Test
    void noArgsConstructor_createsEmpty() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>();

        Assertions.assertNull(valueWithTtl.getValue());
        Assertions.assertNull(valueWithTtl.getTtl());
    }

    /** 对应测试用例 1.3 */
    @Test
    void builder_setsFields() {
        CValueWithTtl<Integer> valueWithTtl = CValueWithTtl.<Integer>builder()
            .value(42)
            .ttl(60L)
            .build();

        Assertions.assertEquals(42, valueWithTtl.getValue());
        Assertions.assertEquals(60L, valueWithTtl.getTtl());
    }

    /** 对应测试用例 1.4 */
    @Test
    void setters_updateFields() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>();

        valueWithTtl.setValue("newValue");
        valueWithTtl.setTtl(200L);

        Assertions.assertEquals("newValue", valueWithTtl.getValue());
        Assertions.assertEquals(200L, valueWithTtl.getTtl());
    }

    /** 对应测试用例 1.5 */
    @Test
    void toString_containsFields() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>("value", 100L);

        String str = valueWithTtl.toString();
        Assertions.assertTrue(str.contains("value"));
        Assertions.assertTrue(str.contains("ttl"));
        Assertions.assertTrue(str.contains("100"));
    }

}
