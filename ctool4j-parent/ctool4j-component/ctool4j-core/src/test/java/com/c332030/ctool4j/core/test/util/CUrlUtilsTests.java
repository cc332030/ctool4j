package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CUrlUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CUrlUtilsTests
 * </p>
 *
 * @since 2025/12/22
 */
public class CUrlUtilsTests {

    static final String DEFAULT_DOMAIN = "https://c332030.com";

    static final String DEFAULT_DOMAIN2 = "https://cc332030.com";

    @Test
    public void getPath() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals("/ip", CUrlUtils.getPath(url));

    }

    @Test
    public void replaceDomain() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals(DEFAULT_DOMAIN2 + "/ip", CUrlUtils.replaceDomain(url, DEFAULT_DOMAIN2));

    }

}
