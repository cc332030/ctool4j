package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.config.CPageConfig;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CReflectUtilsTests
 * </p>
 *
 * @since 2026/6/16
 */
public class CReflectUtilsTests {

    @Test
    public void getAnnotationCached() {

        val anno = CReflectUtils.getAnnotationCached(CPageConfig.class, ConfigurationProperties.class);
        Assertions.assertInstanceOf(ConfigurationProperties.class, anno);

    }

}
