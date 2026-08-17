package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import springfox.documentation.spi.DocumentationType;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * Description: CNotEmptyAnnotationPluginTests
 * </p>
 *
 * @since 2026/8/14
 */
class CNotEmptyAnnotationPluginTests {

    private final CNotEmptyAnnotationPlugin plugin = new CNotEmptyAnnotationPlugin();

    @Test
    void getAnnotationClass() {
        Assertions.assertEquals(NotEmpty.class, plugin.getAnnotationClass());
    }

    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
    }

    @Test
    void supports_null() {
        Assertions.assertTrue(plugin.supports(null));
    }

}
