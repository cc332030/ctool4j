package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.util.CRequestLogUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRequestLogUtilsTests
 * </p>
 *
 * <p>覆盖不依赖 Spring 容器的纯逻辑/ThreadLocal 方法；
 * isEnable/isExcludeUri/genRequestLog/logWrite 等依赖容器或配置对象，不在本测试覆盖范围</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CRequestLogUtilsTests {

    // ---------- EMPTY_REQ（无请求体占位，req 统一为 Object 直接存字符串） ----------

    @Test
    public void emptyReqs() {
        // 正例：占位为固定字符串，与 feign 场景 req 存字符串语义一致
        Assertions.assertEquals("[no request body]", CRequestLogUtils.EMPTY_REQ);
    }

    // ---------- getOpt / getOptThenRemove / remove（ThreadLocal 空场景） ----------

    @Test
    public void getOpt_empty() {
        // 边界：ThreadLocal 未初始化时返回 empty Opt
        CRequestLogUtils.remove();
        val opt = CRequestLogUtils.getOpt();
        Assertions.assertNotNull(opt);
        Assertions.assertTrue(opt.isEmpty());
    }

    @Test
    public void getOptThenRemove_empty() {
        // 边界：ThreadLocal 为空时 getOptThenRemove 返回 empty 且不抛异常
        CRequestLogUtils.remove();
        val opt = CRequestLogUtils.getOptThenRemove();
        Assertions.assertNotNull(opt);
        Assertions.assertTrue(opt.isEmpty());
    }

    @Test
    public void remove_idempotent() {
        // 边界：重复 remove 不抛异常（幂等）
        CRequestLogUtils.remove();
        CRequestLogUtils.remove();
        Assertions.assertTrue(CRequestLogUtils.getOpt().isEmpty());
    }

    // ---------- 常量 ----------

    @Test
    public void constants() {
        // 正例：常量定义合理
        Assertions.assertEquals("request-log", CRequestLogUtils.REQUEST_LOG_STR);
    }

}
