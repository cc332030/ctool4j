package com.c332030.ctool4j.core.test.enums;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CProfileEnumTests
 * </p>
 *
 * @since 2026/1/14
 */
public class CProfileEnumTests {

    @Test
    public void of() {

        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("default"));
        Assertions.assertEquals(CProfileEnum.DEFAULT, CProfileEnum.of("DEFAULT"));

    }

}
