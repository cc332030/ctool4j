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
 * <p>`com.c332030.ctool4j.web.util.CTraceUtils`（CTraceUtils）的测试用例</p>
 *
 * <p>补充覆盖 generateTraceId/setTraceId/getTraceId/removeTraceId/removeTraceInfo 等
 * 不依赖 Spring 容器的链路追踪方法；initTrace 依赖容器请求对象，不在本测试覆盖范围</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 getTraceInfo 返回默认提供者的追踪信息类型。</li>
 *   <li>补充覆盖不依赖 Spring 容器的链路追踪方法：generateTraceId 生成/唯一性、setTraceId/getTraceId 读写与 null 边界、removeTraceId、removeTraceInfo。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认提供者返回 CTraceInfo、generateTraceId 形如 objectId + "-1"、MDC/ThreadLocal 存取的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getTraceInfo 返回 CTraceInfo 类型；generateTraceId 生成及唯一性；setTraceId/getTraceId 写入读取与 null 边界；removeTraceId 移除；removeTraceInfo 移除后仍能取默认实例。</li>
 *   <li>未覆盖：initTrace（依赖请求上下文/MDC，未在单测覆盖）。</li>
 * </ul>
 * <h2>追踪信息</h2>
 * <ul>
 *   <li>1.1 getTraceInfo：返回 CTraceInfo 实例（getTraceInfo）</li>
 *   <li>1.2 generateTraceId：生成形如 objectId + "-1" 的 traceId（generateTraceId）</li>
 *   <li>1.3 generateTraceId 唯一性：连续生成不重复（generateTraceId_unique）</li>
 *   <li>1.4 setTraceId/getTraceId：写入后能读回（setTraceId_getTraceId）</li>
 *   <li>1.5 setTraceId null：设置 null 后 getTraceId 返回 null（setTraceId_null）</li>
 *   <li>1.6 removeTraceId：移除后 getTraceId 为 null（removeTraceId）</li>
 *   <li>1.7 removeTraceInfo：移除后仍能获取默认实例（removeTraceInfo）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
@CustomLog
public class CTraceUtilsTests {

    // ---------- generateTraceId ----------

    /**
     * 对应测试用例 1.2：生成形如 objectId + "-1" 的 traceId
     */
    @Test
    public void generateTraceId() {
        // 正例：生成形如 objectId + "-1" 的 traceId
        val traceId = CTraceUtils.generateTraceId();
        Assertions.assertNotNull(traceId);
        Assertions.assertTrue(traceId.endsWith("-1"));
        // objectId 为 24 位十六进制，加后缀 "-1"
        Assertions.assertTrue(traceId.length() > "-1".length());
    }

    /**
     * 对应测试用例 1.3：generateTraceId 唯一性：连续生成不重复
     */
    @Test
    public void generateTraceId_unique() {
        // 正例：连续生成不重复
        val t1 = CTraceUtils.generateTraceId();
        val t2 = CTraceUtils.generateTraceId();
        Assertions.assertNotEquals(t1, t2);
    }

    // ---------- setTraceId / getTraceId ----------

    /**
     * 对应测试用例 1.4：setTraceId/getTraceId：写入后能读回
     */
    @Test
    public void setTraceId_getTraceId() {
        // 正例：设置后能读回
        CTraceUtils.setTraceId("trace-abc");
        Assertions.assertEquals("trace-abc", CTraceUtils.getTraceId());
        // 清理，避免污染后续用例
        CTraceUtils.removeTraceId();
    }

    /**
     * 对应测试用例 1.5：setTraceId null：设置 null 后 getTraceId 返回 null
     */
    @Test
    public void setTraceId_null() {
        // 边界：设置 null 后 getTraceId 返回 null
        CTraceUtils.setTraceId(null);
        Assertions.assertNull(CTraceUtils.getTraceId());
        CTraceUtils.removeTraceId();
    }

    // ---------- removeTraceId ----------

    /**
     * 对应测试用例 1.6：移除后 getTraceId 为 null
     */
    @Test
    public void removeTraceId() {
        // 正例：移除后 getTraceId 为 null
        CTraceUtils.setTraceId("trace-x");
        CTraceUtils.removeTraceId();
        Assertions.assertNull(CTraceUtils.getTraceId());
    }

    // ---------- getTraceInfo / removeTraceInfo ----------

    /**
     * 对应测试用例 1.1：返回 CTraceInfo 实例
     */
    @Test
    public void getTraceInfo() {
        // 正例：返回 CTraceInfo 类型
        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertNotNull(traceInfo);
        Assertions.assertEquals(CTraceInfo.class, traceInfo.getClass());
    }

    /**
     * 对应测试用例 1.7：移除后仍能获取默认实例
     */
    @Test
    public void removeTraceInfo() {
        // 边界：移除后再次 getTraceInfo 仍能获取默认实例（withInitial 提供者）
        CTraceUtils.removeTraceInfo();
        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertNotNull(traceInfo);
    }

}
