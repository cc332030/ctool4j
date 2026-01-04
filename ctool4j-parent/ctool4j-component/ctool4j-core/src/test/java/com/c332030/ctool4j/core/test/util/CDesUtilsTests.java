package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CDesUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDesUtilsTests
 * </p>
 *
 * @since 2026/1/4
 */
public class CDesUtilsTests {

    private static final String KEY = "Q9ucdr1x";

    private static final String PLAIN_TEXT = "332030";

    private static final String CIPHER_TEXT = "NIAMvCzxs+A=";

    @Test
    public void encrypt() {

        val cipherText = CDesUtils.encrypt(KEY, PLAIN_TEXT);
        Assertions.assertEquals(CIPHER_TEXT, cipherText);

    }

    @Test
    public void decrypt() {

        val plainText = CDesUtils.decrypt(KEY, CIPHER_TEXT);
        Assertions.assertEquals(PLAIN_TEXT, plainText);

    }

}
