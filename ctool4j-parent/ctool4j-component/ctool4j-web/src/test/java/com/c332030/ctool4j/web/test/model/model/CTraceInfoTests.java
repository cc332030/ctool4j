package com.c332030.ctool4j.web.test.model.model;

import com.c332030.ctool4j.web.model.model.CTraceInfo;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTraceInfoTests
 * </p>
 *
 * <p>覆盖 CTraceInfo 数据类：各构造方式与读写</p>
 *
 * @since 2026/8/16
 */
public class CTraceInfoTests {

    @Test
    public void noArgsConstructor() {
        val info = new CTraceInfo();

        Assertions.assertNull(info.getTraceId());
    }

    @Test
    public void allArgsConstructor() {
        val info = new CTraceInfo("trace-1");

        Assertions.assertEquals("trace-1", info.getTraceId());
    }

    @Test
    public void builder() {
        val info = CTraceInfo.builder()
            .traceId("trace-1")
            .build();

        Assertions.assertEquals("trace-1", info.getTraceId());
    }

    @Test
    public void setterAndGetter() {
        val info = new CTraceInfo();
        info.setTraceId("trace-2");

        Assertions.assertEquals("trace-2", info.getTraceId());
    }

}
