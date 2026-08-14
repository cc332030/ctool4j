package com.c332030.ctool4j.definition.test.model.result.impl;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CStrResultTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CStrResultTests {

    @Test
    public void noArgsConstructor() {

        CStrResult<String> result = new CStrResult<>();

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void allArgsConstructor() {

        CStrResult<String> result = new CStrResult<>("200", "OK", "data");

        Assertions.assertEquals("200", result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstance() {

        CStrResult<String> result = CStrResult.newInstance("200", "OK", "data");

        Assertions.assertEquals("200", result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void newInstanceNullParams() {

        CStrResult<String> result = CStrResult.newInstance(null, null, null);

        Assertions.assertNull(result.getCode());
        Assertions.assertNull(result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void success() {

        CStrResult<String> result = CStrResult.success();

        Assertions.assertEquals("200", result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void successWithData() {

        CStrResult<String> result = CStrResult.success("data");

        Assertions.assertEquals("200", result.getCode());
        Assertions.assertEquals("OK", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void successWithHttpStatus() {

        CStrResult<String> result = CStrResult.success(HttpStatus.CREATED, "data");

        Assertions.assertEquals("201", result.getCode());
        Assertions.assertEquals("Created", result.getMessage());
        Assertions.assertEquals("data", result.getData());

    }

    @Test
    public void errorWithMessage() {

        CStrResult<String> result = CStrResult.error("error msg");

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("error msg", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorNullMessage() {

        CStrResult<String> result = CStrResult.error((String)null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("Internal Server Error", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithCodeAndMessage() {

        CStrResult<String> result = CStrResult.error("400", "bad request");

        Assertions.assertEquals("400", result.getCode());
        Assertions.assertEquals("bad request", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithHttpStatusAndMessage() {

        CStrResult<String> result = CStrResult.error(HttpStatus.NOT_FOUND, null);

        Assertions.assertEquals("404", result.getCode());
        Assertions.assertEquals("Not Found", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void errorWithHttpStatusOnly() {

        CStrResult<String> result = CStrResult.error(HttpStatus.FORBIDDEN);

        Assertions.assertEquals("403", result.getCode());
        Assertions.assertEquals("Forbidden", result.getMessage());
        Assertions.assertNull(result.getData());

    }

    @Test
    public void builder() {

        CStrResult<String> result = CStrResult.<String>builder()
            .code("404")
            .message("not found")
            .data("payload")
            .build();

        Assertions.assertEquals("404", result.getCode());
        Assertions.assertEquals("not found", result.getMessage());
        Assertions.assertEquals("payload", result.getData());

    }

}
