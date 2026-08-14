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
 *
 * <p>覆盖枚举 getHeaderName（下划线转 Header 名）与 getText 描述</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CRequestHeaderEnumTests {

    // ---------- getHeaderName ----------

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

    @Test
    public void getHeaderName_singleWord() {
        // 边界：单段常量（无下划线）转大写首字母
        Assertions.assertEquals("Authorization", CRequestHeaderEnum.AUTHORIZATION.getHeaderName());
        Assertions.assertEquals("Accept", CRequestHeaderEnum.ACCEPT.getHeaderName());
    }

    // ---------- getText ----------

    @Test
    public void getText() {
        // 正例：描述文本
        Assertions.assertEquals("鉴权", CRequestHeaderEnum.AUTHORIZATION.getText());
        Assertions.assertEquals("链路追踪ID", CRequestHeaderEnum.X_TRACE_ID.getText());
    }

    // ---------- 枚举完整性 ----------

    @Test
    public void enumValues() {
        // 正例：枚举值数量与顺序稳定
        val values = CRequestHeaderEnum.values();
        Assertions.assertEquals(7, values.length);
        Assertions.assertEquals(CRequestHeaderEnum.AUTHORIZATION, values[0]);
    }

    @Test
    public void valueOf() {
        // 正例：按名称取枚举
        Assertions.assertSame(CRequestHeaderEnum.X_USER_ID, CRequestHeaderEnum.valueOf("X_USER_ID"));
    }

    @Test
    public void valueOf_invalid_throws() {
        // 异常路径：非法名称抛 IllegalArgumentException
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CRequestHeaderEnum.valueOf("NOT_EXISTS")
        );
    }

}
