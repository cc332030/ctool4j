package com.c332030.ctool4j.web.test.spi;

import com.c332030.ctool4j.web.model.model.CTraceInfo;
import com.c332030.ctool4j.web.spi.ICTraceInfoProvider;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CTraceInfoProviderTests
 * </p>
 *
 * <p>覆盖 ICTraceInfoProvider 契约：每次调用返回独立实例</p>
 *
 * @since 2026/8/16
 */

public class CTraceInfoProviderTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getTraceInfo() {
        ICTraceInfoProvider<CTraceInfo> provider = () -> new CTraceInfo("trace-1");

        val first = provider.getTraceInfo();
        val second = provider.getTraceInfo();

        Assertions.assertEquals("trace-1", first.getTraceId());
        Assertions.assertNotSame(first, second);
    }

}
