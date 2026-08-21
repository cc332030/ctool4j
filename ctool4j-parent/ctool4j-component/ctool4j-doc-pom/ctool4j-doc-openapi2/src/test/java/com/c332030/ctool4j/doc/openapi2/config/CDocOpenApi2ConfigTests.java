package com.c332030.ctool4j.doc.openapi2.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDocOpenApi2ConfigTests
 * </p>
 *
 * @since 2026/8/14
 *
 * <p>
 * 是 {@link CDocOpenApi2Config} 的测试用例（对应测试文档 <code>doc/design/openapi2/CDocOpenApi2ConfigTests.adoc</code>）。
 * </p>
 */
class CDocOpenApi2ConfigTests {

    /**
     * <p>
     * 对应测试用例 1.1
     */
    @Test
    void defaultPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        Assertions.assertEquals("/", config.getPathMapping());
    }

    /**
     * <p>
     * 对应测试用例 1.2
     */
    @Test
    void setPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping("/api");
        Assertions.assertEquals("/api", config.getPathMapping());
    }

    /**
     * <p>
     * 对应测试用例 1.3
     */
    @Test
    void setPathMapping_null() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping(null);
        Assertions.assertNull(config.getPathMapping());
    }

}
