package com.c332030.ctool4j.definition.test.model.result.impl;

import com.c332030.ctool4j.definition.model.result.impl.CIntResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CIntResultTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 成功工厂 / 失败工厂 / builder」多个维度组织，验证工厂方法与字段。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 success/error、message null 兜底的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造/全参构建；newInstance（含 null）；success（无数据/有数据/指定状态）；error（默认/自定义 code/HttpStatus）。</li>
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
 *   <li>2.3 success(httpStatus, data)（successWithHttpStatus）</li>
 * </ul>
 * <h2>失败工厂</h2>
 * <ul>
 *   <li>3.1 error(message)（errorWithMessage）</li>
 *   <li>3.2 error(null)（errorNullMessage）</li>
 *   <li>3.3 error(code, message)（errorWithCodeAndMessage）</li>
 *   <li>3.4 error(httpStatus, message)（errorWithHttpStatusAndMessage）</li>
 *   <li>3.5 error(httpStatus)（errorWithHttpStatusOnly）</li>
 * </ul>
 * <h2>builder</h2>
 * <ul>
 *   <li>4.1 builder（builder）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CIntResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    /**
     * 对应测试用例 1.1：无参构造
     */
    @Test
    public void noArgsConstructor() {

        CIntResult<String> result = new CIntResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 1.2：全参构建（统一使用 builder，禁止依赖 lombok 生成的全参构造器）
     */
    @Test
    public void builderAllFields() {

        CIntResult<String> result = CIntResult.<String>builder()
            .code(200)
            .message("OK")
            .data("data")
            .build();

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.3：newInstance
     */
    @Test
    public void newInstance() {

        CIntResult<String> result = CIntResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.4：newInstance null 参数
     */
    @Test
    public void newInstanceNullParams() {

        CIntResult<String> result = CIntResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.1：success()
     */
    @Test
    public void success() {

        CIntResult<String> result = CIntResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.2：success(data)
     */
    @Test
    public void successWithData() {

        CIntResult<String> result = CIntResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 2.3：success(httpStatus, data)
     */
    @Test
    public void successWithHttpStatus() {

        CIntResult<String> result = CIntResult.success(HttpStatus.CREATED, "data");

        Assertions.assertEquals(201, result.getCode());
        Assertions.assertEquals("Created", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 3.1：error(message)
     */
    @Test
    public void errorWithMessage() {

        CIntResult<String> result = CIntResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.2：error(null)
     */
    @Test
    public void errorNullMessage() {

        CIntResult<String> result = CIntResult.error((String)null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.3：error(code, message)
     */
    @Test
    public void errorWithCodeAndMessage() {

        CIntResult<String> result = CIntResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.4：error(httpStatus, message)
     */
    @Test
    public void errorWithHttpStatusAndMessage() {

        CIntResult<String> result = CIntResult.error(HttpStatus.NOT_FOUND, null);

        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("Not Found", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.5：error(httpStatus)
     */
    @Test
    public void errorWithHttpStatusOnly() {

        CIntResult<String> result = CIntResult.error(HttpStatus.FORBIDDEN);

        Assertions.assertEquals(403, result.getCode());
        Assertions.assertEquals("Forbidden", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 4.1：builder
     */
    @Test
    public void builder() {

        CIntResult<String> result = CIntResult.<String>builder()
            .code(404)
            .message("not found")
            .data("payload")
            .build();

        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("not found", result.getMessage());
        Assertions.assertEquals("payload", result.getData());

    }

}
