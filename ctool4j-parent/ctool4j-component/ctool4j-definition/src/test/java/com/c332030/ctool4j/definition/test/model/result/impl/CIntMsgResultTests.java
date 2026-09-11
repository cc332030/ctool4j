package com.c332030.ctool4j.definition.test.model.result.impl;

import com.c332030.ctool4j.definition.model.result.impl.CIntMsgResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIntMsgResultTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 成功工厂 / 失败工厂 / builder / equals」多个维度组织，验证工厂方法与字段。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 success/error、message null 兜底的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造/全参构建；newInstance（含 null）；success；error（默认/自定义 code）；builder；equals/hashCode。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 无参构造（noArgsConstructor）</li>
 *   <li>1.2 全参构建（builderAllFields）</li>
 *   <li>1.3 newInstance（newInstance）</li>
 *   <li>1.4 newInstance null 参数（newInstanceNullParams）</li>
 * </ul>
 * <h2>成功工厂</h2>
 * <ul>
 *   <li>2.1 success()（success）</li>
 *   <li>2.2 success(data)（successWithData）</li>
 * </ul>
 * <h2>失败工厂</h2>
 * <ul>
 *   <li>3.1 error(message)（errorWithMessage）</li>
 *   <li>3.2 error(null)（errorNullMessage）</li>
 *   <li>3.3 error(code, message)（errorWithCodeAndMessage）</li>
 * </ul>
 * <h2>builder 与 equals</h2>
 * <ul>
 *   <li>4.1 builder（builder）</li>
 *   <li>4.2 equals/hashCode（equalsAndHashCode）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CIntMsgResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    /**
     * 对应测试用例 1.1：无参构造
     */
    @Test
    public void noArgsConstructor() {

        CIntMsgResult<String> result = new CIntMsgResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 1.2：全参构建（统一使用 builder，禁止依赖 lombok 生成的全参构造器）
     */
    @Test
    public void builderAllFields() {

        CIntMsgResult<String> result = CIntMsgResult.<String>builder()
            .code(200)
            .msg("OK")
            .data("data")
            .build();

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.3：newInstance
     */
    @Test
    public void newInstance() {

        CIntMsgResult<String> result = CIntMsgResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.4：newInstance null 参数
     */
    @Test
    public void newInstanceNullParams() {

        CIntMsgResult<String> result = CIntMsgResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.1：success()
     */
    @Test
    public void success() {

        CIntMsgResult<String> result = CIntMsgResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.2：success(data)
     */
    @Test
    public void successWithData() {

        CIntMsgResult<String> result = CIntMsgResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 3.1：error(message)
     */
    @Test
    public void errorWithMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.2：error(null)
     */
    @Test
    public void errorNullMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error(null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.3：error(code, message)
     */
    @Test
    public void errorWithCodeAndMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 4.1：builder
     */
    @Test
    public void builder() {

        CIntMsgResult<String> result = CIntMsgResult.<String>builder()
            .code(404)
            .msg("not found")
            .data("payload")
            .build();

        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("not found", result.getMsg());
        Assertions.assertEquals("payload", result.getData());

    }

    /**
     * 对应测试用例 4.2：equals/hashCode
     */
    @Test
    public void equalsAndHashCode() {

        CIntMsgResult<String> a = CIntMsgResult.success("x");
        CIntMsgResult<String> b = CIntMsgResult.success("x");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

    }

}
