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

    private static final String CIPHER_TEXT_64 = "NIAMvCzxs+A=";

    private static final String CIPHER_TEXT_62 = "4VSLrykWrrM";

    /**
     * 测试 DES 加密为 Base64 字符串
     * 对应测试用例 1.1
     */
    @Test
    public void encryptStr64() {

        val cipherText = CDesUtils.encryptStr64(KEY, PLAIN_TEXT);
        Assertions.assertEquals(CIPHER_TEXT_64, cipherText);

    }

    /**
     * 测试 DES 加密为 Base62 字符串
     * 对应测试用例 1.2
     */
    @Test
    public void encryptStr62() {

        val cipherText = CDesUtils.encryptStr62(KEY, PLAIN_TEXT);
        Assertions.assertEquals(CIPHER_TEXT_62, cipherText);

    }

    /**
     * 测试 DES 解密 Base64 字符串
     * 对应测试用例 2.1
     */
    @Test
    public void decryptStr64() {

        val plainText = CDesUtils.decryptStr64(KEY, CIPHER_TEXT_64);
        Assertions.assertEquals(PLAIN_TEXT, plainText);

    }

    /**
     * 测试 DES 解密 Base62 字符串
     * 对应测试用例 2.2
     */
    @Test
    public void decryptStr62() {

        val plainText = CDesUtils.decryptStr62(KEY, CIPHER_TEXT_62);
        Assertions.assertEquals(PLAIN_TEXT, plainText);

    }

}
