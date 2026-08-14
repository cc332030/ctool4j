package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.model.model.CTraceInfo;
import com.c332030.ctool4j.web.util.CTraceUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTraceUtilsTests
 * </p>
 *
 * <p>补充覆盖 generateTraceId/setTraceId/getTraceId/removeTraceId/removeTraceInfo 等
 * 不依赖 Spring 容器的链路追踪方法；initTrace 依赖容器请求对象，不在本测试覆盖范围</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CTraceUtilsTests {

    // ---------- generateTraceId ----------

    @Test
    public void generateTraceId() {
        // 正例：生成形如 objectId + "-1" 的 traceId
        val traceId = CTraceUtils.generateTraceId();
        Assertions.assertNotNull(traceId);
        Assertions.assertTrue(traceId.endsWith("-1"));
        // objectId 为 24 位十六进制，加后缀 "-1"
        Assertions.assertTrue(traceId.length() > "-1".length());
    }

    @Test
    public void generateTraceId_unique() {
        // 正例：连续生成不重复
        val t1 = CTraceUtils.generateTraceId();
        val t2 = CTraceUtils.generateTraceId();
        Assertions.assertNotEquals(t1, t2);
    }

    // ---------- setTraceId / getTraceId ----------

    @Test
    public void setTraceId_getTraceId() {
        // 正例：设置后能读回
        CTraceUtils.setTraceId("trace-abc");
        Assertions.assertEquals("trace-abc", CTraceUtils.getTraceId());
        // 清理，避免污染后续用例
        CTraceUtils.removeTraceId();
    }

    @Test
    public void setTraceId_null() {
        // 边界：设置 null 后 getTraceId 返回 null
        CTraceUtils.setTraceId(null);
        Assertions.assertNull(CTraceUtils.getTraceId());
        CTraceUtils.removeTraceId();
    }

    // ---------- removeTraceId ----------

    @Test
    public void removeTraceId() {
        // 正例：移除后 getTraceId 为 null
        CTraceUtils.setTraceId("trace-x");
        CTraceUtils.removeTraceId();
        Assertions.assertNull(CTraceUtils.getTraceId());
    }

    // ---------- getTraceInfo / removeTraceInfo ----------

    @Test
    public void getTraceInfo() {
        // 正例：返回 CTraceInfo 类型
        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertNotNull(traceInfo);
        Assertions.assertEquals(CTraceInfo.class, traceInfo.getClass());
    }

    @Test
    public void removeTraceInfo() {
        // 边界：移除后再次 getTraceInfo 仍能获取默认实例（withInitial 提供者）
        CTraceUtils.removeTraceInfo();
        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertNotNull(traceInfo);
    }

}
