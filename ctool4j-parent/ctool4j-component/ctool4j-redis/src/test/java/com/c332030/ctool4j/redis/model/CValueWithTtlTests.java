package com.c332030.ctool4j.redis.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CValueWithTtlTests
 * </p>
 *
 * <p>
 * 是 {@link CValueWithTtl} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 lombok 生成的构造器/Builder/Setter 对 value、ttl 字段的读写，以及 toString 包含字段。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对纯数据类（lombok 生成）的约定：全参构建/无参构造、Builder、setter、toString。</li>
 *   <li>依据白盒原则：各构造/赋值路径均取代表性值验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：全参构建、无参构造、Builder、setter、toString。</li>
 *   <li>未覆盖：equals/hashCode（lombok @Data 生成，未单测）；TTL 数值语义（由 Redis 端决定）。</li>
 * </ul>
 * <h2>字段读写</h2>
 * <ul>
 *   <li>1.1 全参构建赋值（builderAllFields_setsFields）</li>
 *   <li>1.2 无参构造为空（noArgsConstructor_createsEmpty）</li>
 *   <li>1.3 Builder 赋值（builder_setsFields）</li>
 *   <li>1.4 setter 更新字段（setters_updateFields）</li>
 *   <li>1.5 toString 含字段（toString_containsFields）</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
public class CValueWithTtlTests {

    /**
     * 对应测试用例 1.1：全参构建（统一使用 builder，禁止依赖 lombok 生成的全参构造器）
     */
    @Test
    void builderAllFields_setsFields() {
        CValueWithTtl<String> valueWithTtl = CValueWithTtl.<String>builder()
            .value("value")
            .ttl(100L)
            .build();

        Assertions.assertEquals("value", valueWithTtl.getValue());
        Assertions.assertEquals(100L, valueWithTtl.getTtl());
    }

    /**
     * 对应测试用例 1.2：无参构造为空
     */
    @Test
    void noArgsConstructor_createsEmpty() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>();

        Assertions.assertNull(valueWithTtl.getValue());
        Assertions.assertNull(valueWithTtl.getTtl());
    }

    /**
     * 对应测试用例 1.3：Builder 赋值
     */
    @Test
    void builder_setsFields() {
        CValueWithTtl<Integer> valueWithTtl = CValueWithTtl.<Integer>builder()
            .value(42)
            .ttl(60L)
            .build();

        Assertions.assertEquals(42, valueWithTtl.getValue());
        Assertions.assertEquals(60L, valueWithTtl.getTtl());
    }

    /**
     * 对应测试用例 1.4：setter 更新字段
     */
    @Test
    void setters_updateFields() {
        CValueWithTtl<String> valueWithTtl = new CValueWithTtl<>();

        valueWithTtl.setValue("newValue");
        valueWithTtl.setTtl(200L);

        Assertions.assertEquals("newValue", valueWithTtl.getValue());
        Assertions.assertEquals(200L, valueWithTtl.getTtl());
    }

    /**
     * 对应测试用例 1.5：toString 含字段
     */
    @Test
    void toString_containsFields() {
        CValueWithTtl<String> valueWithTtl = CValueWithTtl.<String>builder()
            .value("value")
            .ttl(100L)
            .build();

        String str = valueWithTtl.toString();
        Assertions.assertTrue(str.contains("value"));
        Assertions.assertTrue(str.contains("ttl"));
        Assertions.assertTrue(str.contains("100"));
    }

}
