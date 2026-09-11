package com.c332030.ctool4j.doc.openapi2.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDocOpenApi2ConfigTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 pathMapping 的默认值、setter 正常赋值、setter 置 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"pathMapping 默认 /"的约定。</li>
 *   <li>依据黑盒原则：默认值、正常赋值、null 赋值均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认值、设置值、置 null。</li>
 *   <li>未覆盖：{@code @ConfigurationProperties} 从配置文件绑定的端到端行为（需 Spring 上下文）。</li>
 * </ul>
 * <h2>pathMapping</h2>
 * <ul>
 *   <li>1.1 默认值为 "/"（defaultPathMapping）</li>
 *   <li>1.2 set 后生效（setPathMapping）</li>
 *   <li>1.3 set null 后为 null（setPathMapping_null）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CDocOpenApi2Config} 的测试用例。
 * </p>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CDocOpenApi2ConfigTests {

    /**
     * <p>
     * 对应测试用例 1.1：默认值为 "/"
     */
    @Test
    void defaultPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        Assertions.assertEquals("/", config.getPathMapping());
    }

    /**
     * <p>
     * 对应测试用例 1.2：set 后生效
     */
    @Test
    void setPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping("/api");
        Assertions.assertEquals("/api", config.getPathMapping());
    }

    /**
     * <p>
     * 对应测试用例 1.3：set null 后为 null
     */
    @Test
    void setPathMapping_null() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping(null);
        Assertions.assertNull(config.getPathMapping());
    }

}
