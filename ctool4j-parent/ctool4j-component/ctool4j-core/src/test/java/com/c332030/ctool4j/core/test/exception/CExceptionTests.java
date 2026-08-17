package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CExceptionTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CExceptionTests {

    @Test
    public void defaultConstructor() {

        CException ex = new CException();
        Assertions.assertNull(ex.getMessage());
        Assertions.assertNull(ex.getCause());

    }

    @Test
    public void messageConstructor() {

        CException ex = new CException("boom");
        Assertions.assertEquals("boom", ex.getMessage());

    }

    @Test
    public void causeConstructor() {

        Throwable cause = new IllegalStateException("cause");
        CException ex = new CException(cause);
        Assertions.assertSame(cause, ex.getCause());

    }

    @Test
    public void messageAndCauseConstructor() {

        Throwable cause = new IllegalStateException("cause");
        CException ex = new CException("boom", cause);
        Assertions.assertEquals("boom", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());

    }

}
