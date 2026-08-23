package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CBase62Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CBase62UtilsTests
 * </p>
 *
 * @since 2026/1/4
 */
public class CBase62UtilsTests {

    /**
     * 对应测试用例 1.1 / 1.2 / 1.3
     */
    @Test
    public void encode() {

        byte[] origin = "hello".getBytes(StandardCharsets.UTF_8);
        String encoded = CBase62Utils.encode(origin);
        Assertions.assertNotNull(encoded);
        Assertions.assertFalse(encoded.isEmpty());
        Assertions.assertArrayEquals(origin, CBase62Utils.decode(encoded));

        Assertions.assertNull(CBase62Utils.encode(null));
        Assertions.assertNull(CBase62Utils.encode(new byte[0]));

    }

    /**
     * 对应测试用例 2.1 / 2.2 / 2.3
     */
    @Test
    public void decode() {

        byte[] origin = "hello".getBytes(StandardCharsets.UTF_8);
        String encoded = CBase62Utils.encode(origin);
        Assertions.assertArrayEquals(origin, CBase62Utils.decode(encoded));

        Assertions.assertNull(CBase62Utils.decode(null));
        Assertions.assertNull(CBase62Utils.decode(""));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void roundTrip() {

        byte[] origin = "ctool4j-base62".getBytes(StandardCharsets.UTF_8);
        String encoded = CBase62Utils.encode(origin);
        Assertions.assertArrayEquals(origin, CBase62Utils.decode(encoded));

    }

}
