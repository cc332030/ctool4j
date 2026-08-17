package com.c332030.ctool4j.core.test.config;

import com.c332030.ctool4j.core.config.CPageConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CPageConfigTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CPageConfigTests {

    @Test
    public void noArgsConstructor() {

        CPageConfig config = new CPageConfig();

        Assertions.assertEquals(Integer.valueOf(100), config.getDefaultPageSize());

    }

    @Test
    public void setterAndGetter() {

        CPageConfig config = new CPageConfig();
        config.setDefaultPageSize(50);

        Assertions.assertEquals(Integer.valueOf(50), config.getDefaultPageSize());

    }

    @Test
    public void toStringContainsFields() {

        CPageConfig config = new CPageConfig();
        config.setDefaultPageSize(50);

        String str = config.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CPageConfig"));
        Assertions.assertTrue(str.contains("defaultPageSize=50"));

    }

}
