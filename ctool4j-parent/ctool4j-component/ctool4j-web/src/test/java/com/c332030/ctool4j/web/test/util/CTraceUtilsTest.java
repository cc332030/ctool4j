package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.model.model.CTraceInfo;
import com.c332030.ctool4j.web.util.CTraceUtils;
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

    /**
     * 测试获取默认追踪信息
     */
    @Test
    public void getTraceInfo() {

        val traceInfo = CTraceUtils.getTraceInfo();
        Assertions.assertEquals(CTraceInfo.class, traceInfo.getClass());

    }

}
