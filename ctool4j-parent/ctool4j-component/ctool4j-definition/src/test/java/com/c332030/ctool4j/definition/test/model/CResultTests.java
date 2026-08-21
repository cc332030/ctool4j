package com.c332030.ctool4j.definition.test.model;

import com.c332030.ctool4j.definition.model.CResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CResultTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    /**
     * 对应测试用例 1.1
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
     * 对应测试用例 1.2
     */
    @Test
    public void newInstance() {

        CResult<String> result = CResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void newInstanceNullParams() {

        CResult<String> result = CResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void success() {

        CResult<String> result = CResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void successWithData() {

        CResult<String> result = CResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void successNullData() {

        CResult<String> result = CResult.success(null);

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void errorWithMessage() {

        CResult<String> result = CResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void errorNullMessage() {

        CResult<String> result = CResult.error(null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void errorEmptyMessage() {

        CResult<String> result = CResult.error("");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.4
     */
    @Test
    public void errorWithCodeAndMessage() {

        CResult<String> result = CResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.5
     */
    @Test
    public void errorNullCodeAndMessage() {

        CResult<String> result = CResult.error(null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    /**
     * 对应测试用例 3.6
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
     * 对应测试用例 4.1
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
     * 对应测试用例 4.2
     */
    @Test
    public void getMessageDelegateToMsg() {

        CResult<String> result = CResult.error(500, "boom");

        Assertions.assertEquals(result.getMsg(), result.getMessage());

    }

    /**
     * 对应测试用例 5.1
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
