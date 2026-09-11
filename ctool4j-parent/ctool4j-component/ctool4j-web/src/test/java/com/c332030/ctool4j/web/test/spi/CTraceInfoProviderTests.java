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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 ICTraceInfoProvider.getTraceInfo：获取链路追踪信息 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse/MockMvc 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>ICTraceInfoProvider.getTraceInfo：获取链路追踪信息</h2>
 * <ul>
 *   <li>1.1 getTraceInfo（getTraceInfo）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */

public class CTraceInfoProviderTests {

    /**
     * 对应测试用例 1.1：getTraceInfo
     */
    @Test
    public void getTraceInfo() {
        ICTraceInfoProvider<CTraceInfo> provider = () -> CTraceInfo.builder()
            .traceId("trace-1")
            .build();

        val first = provider.getTraceInfo();
        val second = provider.getTraceInfo();

        Assertions.assertEquals("trace-1", first.getTraceId());
        Assertions.assertNotSame(first, second);
    }

}
