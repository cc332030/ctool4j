package com.c332030.ctool4j.definition.test.model.result.impl;

import com.c332030.ctool4j.definition.model.result.impl.CIntMsgResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIntMsgResultTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CIntMsgResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    @Test
    public void noArgsConstructor() {

        CIntMsgResult<String> result = new CIntMsgResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void allArgsConstructor() {

        CIntMsgResult<String> result = new CIntMsgResult<>(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstance() {

        CIntMsgResult<String> result = CIntMsgResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstanceNullParams() {

        CIntMsgResult<String> result = CIntMsgResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMsg());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void success() {

        CIntMsgResult<String> result = CIntMsgResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void successWithData() {

        CIntMsgResult<String> result = CIntMsgResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMsg());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void errorWithMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorNullMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error(null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMsg());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithCodeAndMessage() {

        CIntMsgResult<String> result = CIntMsgResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMsg());
        Assertions.assertNull(result.getData());

    }

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

    @Test
    public void equalsAndHashCode() {

        CIntMsgResult<String> a = CIntMsgResult.success("x");
        CIntMsgResult<String> b = CIntMsgResult.success("x");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

    }

}
