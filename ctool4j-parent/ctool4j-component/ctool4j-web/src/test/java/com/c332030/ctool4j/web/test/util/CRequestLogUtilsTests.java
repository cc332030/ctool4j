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

    // ---------- EMPTY_REQ / EMPTY_RSP（无请求体/响应体占位，统一为字符串） ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void emptyReqs() {
        // 正例：占位为固定字符串，与 feign 场景 req 存字符串语义一致
        Assertions.assertEquals("[no request body]", CRequestLogUtils.EMPTY_REQ);
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void emptyRsp() {
        // 正例：无响应体占位为固定字符串，服务端 MVC 请求日志初始化使用
        Assertions.assertEquals("[no response body]", CRequestLogUtils.EMPTY_RSP);
    }

    // ---------- getOpt / getOptThenRemove / remove（ThreadLocal 空场景） ----------

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void getOpt_empty() {
        // 边界：ThreadLocal 未初始化时返回 empty Opt
        CRequestLogUtils.remove();
        val opt = CRequestLogUtils.getOpt();
        Assertions.assertNotNull(opt);
        Assertions.assertTrue(opt.isEmpty());
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void getOptThenRemove_empty() {
        // 边界：ThreadLocal 为空时 getOptThenRemove 返回 empty 且不抛异常
        CRequestLogUtils.remove();
        val opt = CRequestLogUtils.getOptThenRemove();
        Assertions.assertNotNull(opt);
        Assertions.assertTrue(opt.isEmpty());
    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void remove_idempotent() {
        // 边界：重复 remove 不抛异常（幂等）
        CRequestLogUtils.remove();
        CRequestLogUtils.remove();
        Assertions.assertTrue(CRequestLogUtils.getOpt().isEmpty());
    }

    // ---------- 常量 ----------

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void constants() {
        // 正例：常量定义合理
        Assertions.assertEquals("request-log", CRequestLogUtils.REQUEST_LOG_STR);
    }

}
