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

    // ---------- getRequestBodyMap ----------

    @Test
    public void getRequestBodyMap() {
        // 正例：封装 requestBody 到 map（key 为 REQUEST_BODY）
        val reqBody = new Object() {
            @Override
            public String toString() {
                return "{\"a\":1}";
            }
        };
        val map = CRequestLogUtils.getRequestBodyMap(reqBody);
        Assertions.assertNotNull(map);
        Assertions.assertSame(reqBody, map.get(CRequestLogUtils.REQUEST_BODY));
    }

    @Test
    public void getRequestBodyMap_null() {
        // 边界：null requestBody 也封装进 map
        val map = CRequestLogUtils.getRequestBodyMap(null);
        Assertions.assertNotNull(map);
        Assertions.assertNull(map.get(CRequestLogUtils.REQUEST_BODY));
    }

    @Test
    public void getRequestBodyMap_string() {
        // 正例：字符串 body
        val map = CRequestLogUtils.getRequestBodyMap("hello");
        Assertions.assertEquals("hello", map.get(CRequestLogUtils.REQUEST_BODY));
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
        Assertions.assertEquals("requestBody", CRequestLogUtils.REQUEST_BODY);
    }

}
