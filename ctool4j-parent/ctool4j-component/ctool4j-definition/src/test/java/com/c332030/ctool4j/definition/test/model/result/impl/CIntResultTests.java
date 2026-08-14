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
 * @since 2026/8/14
 */
public class CIntResultTests {

    private static final int OK_CODE = 200;

    private static final int ERROR_CODE = 500;

    @Test
    public void noArgsConstructor() {

        CIntResult<String> result = new CIntResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void allArgsConstructor() {

        CIntResult<String> result = new CIntResult<>(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstance() {

        CIntResult<String> result = CIntResult.newInstance(200, "OK", "data");

        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstanceNullParams() {

        CIntResult<String> result = CIntResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void success() {

        CIntResult<String> result = CIntResult.success();

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void successWithData() {

        CIntResult<String> result = CIntResult.success("data");

        Assertions.assertEquals(OK_CODE, result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void successWithHttpStatus() {

        CIntResult<String> result = CIntResult.success(HttpStatus.CREATED, "data");

        Assertions.assertEquals(201, result.getCode());
        Assertions.assertEquals("Created", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void errorWithMessage() {

        CIntResult<String> result = CIntResult.error("error msg");

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("error msg", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorNullMessage() {

        CIntResult<String> result = CIntResult.error((String)null);

        Assertions.assertEquals(ERROR_CODE, result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithCodeAndMessage() {

        CIntResult<String> result = CIntResult.error(400, "bad request");

        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithHttpStatusAndMessage() {

        CIntResult<String> result = CIntResult.error(HttpStatus.NOT_FOUND, null);

        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("Not Found", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithHttpStatusOnly() {

        CIntResult<String> result = CIntResult.error(HttpStatus.FORBIDDEN);

        Assertions.assertEquals(403, result.getCode());
        Assertions.assertEquals("Forbidden", result.getMessage());
        Assertions.assertNull(result.getData());

    }

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
