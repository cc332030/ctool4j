package com.c332030.ctool4j.web.test.model.model;

import com.c332030.ctool4j.web.model.model.CTraceInfo;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTraceInfoTests
 * </p>
 *
 * <p>覆盖 CTraceInfo 数据类：各构造方式与读写</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / builder / setter/getter」多个维度组织，验证 Lombok @Data/@SuperBuilder 行为。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @Data/@SuperBuilder 生成方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造/全参构建；builder；setter/getter。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>构造与取值</h2>
 * <ul>
 *   <li>1.1 无参构造：traceId 为 null（noArgsConstructor）</li>
 *   <li>1.2 全参构建：traceId 正确（builderAllFields）</li>
 *   <li>1.3 builder：traceId 正确（builder）</li>
 *   <li>1.4 setter/getter：设置与读取（setterAndGetter）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CTraceInfoTests {

    /**
     * 对应测试用例 1.1：无参构造：traceId 为 null
     */
    @Test
    public void noArgsConstructor() {
        val info = new CTraceInfo();

        Assertions.assertNull(info.getTraceId());
    }

    /**
     * 对应测试用例 1.2：全参构建（统一使用 builder，禁止依赖 lombok 生成的全参构造器）
     */
    @Test
    public void builderAllFields() {
        val info = CTraceInfo.builder()
            .traceId("trace-1")
            .build();

        Assertions.assertEquals("trace-1", info.getTraceId());
    }

    /**
     * 对应测试用例 1.3：traceId 正确
     */
    @Test
    public void builder() {
        val info = CTraceInfo.builder()
            .traceId("trace-1")
            .build();

        Assertions.assertEquals("trace-1", info.getTraceId());
    }

    /**
     * 对应测试用例 1.4：setter/getter：设置与读取
     */
    @Test
    public void setterAndGetter() {
        val info = new CTraceInfo();
        info.setTraceId("trace-2");

        Assertions.assertEquals("trace-2", info.getTraceId());
    }

}
