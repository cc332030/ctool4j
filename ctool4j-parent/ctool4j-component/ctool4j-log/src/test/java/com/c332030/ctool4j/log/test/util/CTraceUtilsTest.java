package com.c332030.ctool4j.log.test.util;

import com.c332030.ctool4j.log.model.CTraceInfo;
import com.c332030.ctool4j.log.util.CTraceUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CExceptionUtilsTest
 * </p>
 *
 * @since 2025/9/14
 */
@CustomLog
public class CTraceUtilsTest {

    @Test
    public void newBusinessException() {

        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertEquals(CTraceInfo.class, traceInfo.getClass());

    }

}
