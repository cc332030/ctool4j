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
 * isExcludeUri（依赖配置的用户排除项部分）、isEnable/genRequestLog/logWrite 等依赖容器或配置对象，
 * 不在本测试覆盖范围；isExcludeUri 的内置静态资源排除分支（配置无关，先于配置读取）纳入本测试覆盖</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CRequestLogUtils：请求日志采集与常量 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>{@code isExcludeUri} 的内置静态资源排除分支（先于配置读取、配置无关）纳入覆盖：接口文档静态资源、静态文件扩展名命中即排除，业务 uri 不排除。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：{@code isExcludeUri} 依赖用户配置 {@code exclude-uri-patterns} 的部分、{@code isEnable}/{@code genRequestLog}/{@code logWrite} 等依赖容器装配的场景，由集成测试覆盖。</li>
 * </ul>
 * <h2>CRequestLogUtils：请求日志采集与常量</h2>
 * <ul>
 *   <li>1.1 emptyReqs（emptyReqs）</li>
 *   <li>1.2 emptyRsp（emptyRsp）</li>
 *   <li>1.3 getOpt_empty（getOpt_empty）</li>
 *   <li>1.4 getOptThenRemove_empty（getOptThenRemove_empty）</li>
 *   <li>1.5 remove_idempotent（remove_idempotent）</li>
 *   <li>1.6 constants（constants）</li>
 *   <li>1.7 isExcludeUri：接口文档静态资源默认排除（excludeStaticResource_docEntry）</li>
 *   <li>1.8 isExcludeUri：静态文件扩展名默认排除（excludeStaticResource_byExtension）</li>
 *   <li>1.9 isExcludeUri：业务 uri 不排除（notExclude_businessUri）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */

@CustomLog
public class CRequestLogUtilsTests {

    // ---------- EMPTY_REQ / EMPTY_RSP（无请求体/响应体占位，统一为字符串） ----------

    /**
     * 对应测试用例 1.1：emptyReqs
     */
    @Test
    public void emptyReqs() {
        // 正例：占位为固定字符串，与 feign 场景 req 存字符串语义一致
        Assertions.assertEquals("[no request body]", CRequestLogUtils.EMPTY_REQ);
    }

    /**
     * 对应测试用例 1.2：emptyRsp
     */
    @Test
    public void emptyRsp() {
        // 正例：无响应体占位为固定字符串，服务端 MVC 请求日志初始化使用
        Assertions.assertEquals("[no response body]", CRequestLogUtils.EMPTY_RSP);
    }

    // ---------- getOpt / getOptThenRemove / remove（ThreadLocal 空场景） ----------

    /**
     * 对应测试用例 1.3：getOpt_empty
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
     * 对应测试用例 1.4：getOptThenRemove_empty
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
     * 对应测试用例 1.5：remove_idempotent
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
     * 对应测试用例 1.6：constants
     */
    @Test
    public void constants() {
        // 正例：常量定义合理
        Assertions.assertEquals("request-log", CRequestLogUtils.REQUEST_LOG_STR);
    }

    // ---------- isExcludeUri：内置静态资源默认排除（配置无关分支，先于配置读取） ----------

    /**
     * 对应测试用例 1.7：接口文档静态资源默认排除
     */
    @Test
    public void excludeStaticResource_docEntry() {
        // 正例：接口文档入口静态资源默认排除，无需配置
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/doc.html"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/webjars/css/app.1824bac3.css"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/webjars/js/app.5de26223.js"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/swagger-resources"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/v2/api-docs"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/favicon.ico"));
    }

    /**
     * 对应测试用例 1.8：静态文件扩展名默认排除
     */
    @Test
    public void excludeStaticResource_byExtension() {
        // 正例：常见静态文件扩展名默认排除
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/static/app.js"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/css/main.css"));
        Assertions.assertTrue(CRequestLogUtils.isExcludeUri("/img/logo.png"));
    }

    /**
     * 对应测试用例 1.9：业务 uri 不排除
     */
    @Test
    public void notExclude_businessUri() {
        // 反例：业务接口 uri 不在内置静态资源排除内；未注入配置（null）时返回 false
        Assertions.assertFalse(CRequestLogUtils.isExcludeUri("/api/user/list"));
        Assertions.assertFalse(CRequestLogUtils.isExcludeUri("/c-schema/test"));
    }

}
