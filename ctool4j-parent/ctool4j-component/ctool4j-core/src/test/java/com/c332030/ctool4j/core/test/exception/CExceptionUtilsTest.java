package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CExceptionUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CExceptionUtilsTest
 * </p>
 *
 * @since 2025/9/14
 */
@CustomLog
public class CExceptionUtilsTest {

    @Test
    public void newBusinessException() {

        val ex = CExceptionUtils.newBusinessException("test");
        log.info("business exception", ex);

    }

}
