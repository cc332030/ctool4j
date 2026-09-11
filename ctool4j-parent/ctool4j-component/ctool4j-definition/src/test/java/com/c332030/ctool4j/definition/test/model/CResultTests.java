package com.c332030.ctool4j.definition.test.model;

import com.c332030.ctool4j.definition.model.CResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CResultTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 成功工厂 / 失败工厂 / builder / 委托 / equals」多个维度组织。</li>
 *   <li>构造覆盖无参与 newInstance（null 参数）；成功覆盖无数据/有数据/null 数据；失败覆盖默认 500、null/空消息、</li>
 *   <li>自定义 code；边界覆盖负/零 code。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 success(200)/error(500)、message null 兜底的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造；newInstance（含 null）；success 三形态；error 多形态（默认/自定义 code、null/空消息）；</li>
 *   <li>error code 边界（-1/0）；builder；getMessage 委托；equals/hashCode。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 无参构造：字段 null（noArgsConstructor）</li>
 *   <li>1.2 newInstance：字段正确（newInstance）</li>
 *   <li>1.3 newInstance null 参数：字段 null（newInstanceNullParams）</li>
 * </ul>
 * <h2>成功工厂</h2>
 * <ul>
 *   <li>2.1 success：200/OK/null 数据（success）</li>
 *   <li>2.2 success(data)：200/OK/数据（successWithData）</li>
 *   <li>2.3 success(null)：200/OK/null（successNullData）</li>
 * </ul>
 * <h2>失败工厂</h2>
 * <ul>
 *   <li>3.1 error(message)：500/消息（errorWithMessage）</li>
 *   <li>3.2 error(null)：500/原因短语（errorNullMessage）</li>
 *   <li>3.3 error("")：500/空消息（errorEmptyMessage）</li>
 *   <li>3.4 error(code, message)：自定义 code（errorWithCodeAndMessage）</li>
 *   <li>3.5 error(null, null)：code/msg null（errorNullCodeAndMessage）</li>
 *   <li>3.6 error code 边界：-1/0（errorCodeBoundary）</li>
 * </ul>
 * <h2>builder 与委托</h2>
 * <ul>
 *   <li>4.1 builder：构造正确（builder）</li>
 *   <li>4.2 getMessage 委托 msg（getMessageDelegateToMsg）</li>
 * </ul>
 * <h2>equals/hashCode</h2>
 * <ul>
 *   <li>5.1 同值相等（equalsAndHashCode）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    /**
     * 对应测试用例 1.1：无参构造：字段 null
     */
    @Test
    public void noArgsConstructor() {

        CResult<String> result = new CResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());
        Assertions.assertNull(result.getMessage());

    }

    /**
     * 对应测试用例 1.2：字段正确
     */
    @Test
    public void newInstance() {

        CResult<String> result = CResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.3：newInstance null 参数：字段 null
     */
    @Test
    public void newInstanceNullParams() {

        CResult<String> result = CResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.1：200/OK/null 数据
     */
    @Test
    public void success() {

        CResult<String> result = CResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.2：success(data)：200/OK/数据
     */
    @Test
    public void successWithData() {

        CResult<String> result = CResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 2.3：success(null)：200/OK/null
     */
    @Test
    public void successNullData() {

        CResult<String> result = CResult.success(null);

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.1：error(message)：500/消息
     */
    @Test
    public void errorWithMessage() {

        CResult<String> result = CResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.2：error(null)：500/原因短语
     */
    @Test
    public void errorNullMessage() {

        CResult<String> result = CResult.error(null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.3：error("")：500/空消息
     */
    @Test
    public void errorEmptyMessage() {

        CResult<String> result = CResult.error("");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.4：error(code, message)：自定义 code
     */
    @Test
    public void errorWithCodeAndMessage() {

        CResult<String> result = CResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.5：error(null, null)：code/msg null
     */
    @Test
    public void errorNullCodeAndMessage() {

        CResult<String> result = CResult.error(null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.6：error code 边界：-1/0
     */
    @Test
    public void errorCodeBoundary() {

        CResult<String> negative = CResult.error(-1, "negative");
        Assertions.assertEquals(-1, negative.getCode());
        Assertions.assertEquals("negative", negative.getMsg());

        CResult<String> zero = CResult.error(0, "zero");
        Assertions.assertEquals(0, zero.getCode());
        Assertions.assertEquals("zero", zero.getMsg());

    }

    /**
     * 对应测试用例 4.1：构造正确
     */
    @Test
    public void builder() {

        CResult<String> result = CResult.<String>builder()
            .code(404)
            .msg("not found")
            .data("payload")
            .build();

        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("not found", result.getMsg());
        Assertions.assertEquals("payload", result.getData());

    }

    /**
     * 对应测试用例 4.2：getMessage 委托 msg
     */
    @Test
    public void getMessageDelegateToMsg() {

        CResult<String> result = CResult.error(500, "boom");

        Assertions.assertEquals(result.getMsg(), result.getMessage());

    }

    /**
     * 对应测试用例 5.1：同值相等
     */
    @Test
    public void equalsAndHashCode() {

        CResult<String> a = CResult.success("x");
        CResult<String> b = CResult.success("x");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CResult<String> c = CResult.success("y");
        Assertions.assertNotEquals(a, c);

    }

}
