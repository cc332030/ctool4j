package com.c332030.ctool4j.doc.openapi2.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDocOpenApi2ConfigTests
 * </p>
 *
 * @since 2026/8/14
 */
class CDocOpenApi2ConfigTests {

    @Test
    void defaultPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        Assertions.assertEquals("/", config.getPathMapping());
    }

    @Test
    void setPathMapping() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping("/api");
        Assertions.assertEquals("/api", config.getPathMapping());
    }

    @Test
    void setPathMapping_null() {
        CDocOpenApi2Config config = new CDocOpenApi2Config();
        config.setPathMapping(null);
        Assertions.assertNull(config.getPathMapping());
    }

}
