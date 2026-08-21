package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CBase64Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CBase64UtilsTests
 * </p>
 *
 * @since 2026/1/4
 */
public class CBase64UtilsTests {

    /**
     * 对应测试用例 1.1 / 1.2 / 1.3
     */
    @Test
    public void encode() {

        String encoded = CBase64Utils.encode("hello".getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("aGVsbG8=", encoded);

        Assertions.assertNull(CBase64Utils.encode(null));
        Assertions.assertNull(CBase64Utils.encode(new byte[0]));

    }

    /**
     * 对应测试用例 2.1 / 2.2 / 2.3
     */
    @Test
    public void decode() {

        byte[] decoded = CBase64Utils.decode("aGVsbG8=");
        Assertions.assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), decoded);

        Assertions.assertNull(CBase64Utils.decode(null));
        Assertions.assertNull(CBase64Utils.decode(""));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void roundTrip() {

        byte[] origin = "ctool4j-base64".getBytes(StandardCharsets.UTF_8);
        String encoded = CBase64Utils.encode(origin);
        Assertions.assertArrayEquals(origin, CBase64Utils.decode(encoded));

    }

}
