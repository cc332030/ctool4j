package com.c332030.ctool4j.web.test.enums;

import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRequestHeaderEnumTests
 * </p>
 * <p>`com.c332030.ctool4j.web.enums.CRequestHeaderEnum`（CRequestHeaderEnum）的测试用例</p>
 *
 * <p>覆盖枚举 getHeaderName（下划线转 Header 名）与 getText 描述</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「Header 名 / 描述 / 枚举完整性」多个维度组织。</li>
 *   <li>Header 名覆盖多段下划线、单段（无下划线）；描述覆盖文本；完整性覆盖数量/顺序/valueOf/非法抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 Header 名推导与枚举元素的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：Header 名（多段/单段）；getText；枚举数量/顺序；valueOf 正常/非法抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>Header 名</h2>
 * <ul>
 *   <li>1.1 多段下划线：{@code X_TRACE_ID} → {@code X-Trace-Id} 等（getHeaderName）</li>
 *   <li>1.2 单段：{@code AUTHORIZATION} → {@code Authorization}（getHeaderName_singleWord）</li>
 * </ul>
 * <h2>描述</h2>
 * <ul>
 *   <li>2.1 getText：描述正确（getText）</li>
 * </ul>
 * <h2>枚举完整性</h2>
 * <ul>
 *   <li>3.1 enumValues：7 个枚举、顺序稳定（enumValues）</li>
 *   <li>3.2 valueOf：按名取枚举（valueOf）</li>
 *   <li>3.3 valueOf 非法：抛 IllegalArgumentException（valueOf_invalid_throws）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
@CustomLog
public class CRequestHeaderEnumTests {

    // ---------- getHeaderName ----------

    /**
     * 对应测试用例 1.1：多段下划线：{@code X_TRACE_ID} → {@code X-Trace-Id} 等
     */
    @Test
    public void getHeaderName() {
        // 正例：下划线转中划线 Header 名
        Assertions.assertEquals("Authorization", CRequestHeaderEnum.AUTHORIZATION.getHeaderName());
        Assertions.assertEquals("Accept-Language", CRequestHeaderEnum.ACCEPT_LANGUAGE.getHeaderName());
        Assertions.assertEquals("Accept", CRequestHeaderEnum.ACCEPT.getHeaderName());
        Assertions.assertEquals("X-Real-Ip", CRequestHeaderEnum.X_REAL_IP.getHeaderName());
        Assertions.assertEquals("X-Trace-Id", CRequestHeaderEnum.X_TRACE_ID.getHeaderName());
        Assertions.assertEquals("X-Tenant-Id", CRequestHeaderEnum.X_TENANT_ID.getHeaderName());
        Assertions.assertEquals("X-User-Id", CRequestHeaderEnum.X_USER_ID.getHeaderName());
    }

    /**
     * 对应测试用例 1.2：单段：{@code AUTHORIZATION} → {@code Authorization}
     */
    @Test
    public void getHeaderName_singleWord() {
        // 边界：单段常量（无下划线）转大写首字母
        Assertions.assertEquals("Authorization", CRequestHeaderEnum.AUTHORIZATION.getHeaderName());
        Assertions.assertEquals("Accept", CRequestHeaderEnum.ACCEPT.getHeaderName());
    }

    // ---------- getText ----------

    /**
     * 对应测试用例 2.1：描述正确
     */
    @Test
    public void getText() {
        // 正例：描述文本
        Assertions.assertEquals("鉴权", CRequestHeaderEnum.AUTHORIZATION.getText());
        Assertions.assertEquals("链路追踪ID", CRequestHeaderEnum.X_TRACE_ID.getText());
    }

    // ---------- 枚举完整性 ----------

    /**
     * 对应测试用例 3.1：7 个枚举、顺序稳定
     */
    @Test
    public void enumValues() {
        // 正例：枚举值数量与顺序稳定
        val values = CRequestHeaderEnum.values();
        Assertions.assertEquals(7, values.length);
        Assertions.assertEquals(CRequestHeaderEnum.AUTHORIZATION, values[0]);
    }

    /**
     * 对应测试用例 3.2：按名取枚举
     */
    @Test
    public void valueOf() {
        // 正例：按名称取枚举
        Assertions.assertSame(CRequestHeaderEnum.X_USER_ID, CRequestHeaderEnum.valueOf("X_USER_ID"));
    }

    /**
     * 对应测试用例 3.3：valueOf 非法：抛 IllegalArgumentException
     */
    @Test
    public void valueOf_invalid_throws() {
        // 异常路径：非法名称抛 IllegalArgumentException
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CRequestHeaderEnum.valueOf("NOT_EXISTS")
        );
    }

}
