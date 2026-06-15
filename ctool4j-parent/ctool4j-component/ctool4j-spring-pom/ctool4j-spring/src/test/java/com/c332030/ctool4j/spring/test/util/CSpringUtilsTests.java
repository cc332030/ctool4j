package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CSpringUtilsTests
 * </p>
 *
 * @since 2026/6/2
 */
@CTool4jSpringBootTest
public class CSpringUtilsTests {

    private static final CProfileEnum PROFILE = CProfileEnum.DEV;

    private static final String CLASS_NAME = CSpringUtilsTests.class.getSimpleName();

    @Test
    public void getActiveProfile() {

        val profile = CSpringUtils.getActiveProfile();
        Assertions.assertEquals(PROFILE, profile);
    }

    @Test
    public void getActiveProfileText() {

        val profile = CSpringUtils.getActiveProfileText();
        Assertions.assertEquals(PROFILE.getText(), profile);
    }

    @Test
    public void profilePrefix() {

        val profile = CSpringUtils.profilePrefix(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    @Test
    public void profilePrefixExcludeProd() {

        val profile = CSpringUtils.profilePrefixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    @Test
    public void profileSuffix() {

        val profile = CSpringUtils.profileSuffix(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    @Test
    public void profileSuffixExcludeProd() {

        val profile = CSpringUtils.profileSuffixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    @Test
    public void profileTextSuffix() {

        val profile = CSpringUtils.profileTextSuffix(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + "-" + PROFILE.getText(), profile);
    }

}
