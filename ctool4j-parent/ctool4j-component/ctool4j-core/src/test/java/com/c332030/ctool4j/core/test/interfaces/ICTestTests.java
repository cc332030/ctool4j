package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.ICTest;
import com.c332030.ctool4j.core.util.CTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICTestTests
 * </p>
 *
 * @since 2025/12/12
 */
public class ICTestTests {

    @Test
    public void isTest() {

        ICTest impl = new ICTest() {
        };
        Assertions.assertEquals(CTestUtils.isTest(), impl.isTest());

    }

}
