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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「加密 / 解密」两个维度、再按输出形态（Base64 / Base62）组织。</li>
 *   <li>加密验证确定性结果：以固定 key + 明文断言固定的 Base64/Base62 密文，确保加密结果稳定。</li>
 *   <li>解密以加密得到的固定密文还原明文，验证加解密互为逆操作。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对空入参返回 null 的约定（本批测试用例未单列空入参，见覆盖场景说明）。</li>
 *   <li>依据测试方法（黑盒等价类）：固定样本的确定性加解密结果。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：Base64 加密/解密、Base62 加密/解密四组确定性的加解密往返。</li>
 *   <li>未覆盖：{@code getDES}、{@code encrypt}（字节数组入口）、{@code decrypt}（字节数组入口）、空入参返回 null 分支；</li>
 *   <li>这些入口行为由确定性样本间接保证（字符串入口内部走字节入口），空入参分支与设计约定一致，</li>
 *   <li>属可选扩展用例，后续批次可补充。</li>
 * </ul>
 * <h2>加密</h2>
 * <ul>
 *   <li>1.1 Base64：{@code KEY + "332030"} 加密得到 {@code NIAMvCzxs+A=}（encryptStr64）</li>
 *   <li>1.2 Base62：{@code KEY + "332030"} 加密得到 {@code 4VSLrykWrrM}（encryptStr62）</li>
 * </ul>
 * <h2>解密</h2>
 * <ul>
 *   <li>2.1 Base64：{@code KEY + NIAMvCzxs+A=} 解密还原 {@code 332030}（decryptStr64）</li>
 *   <li>2.2 Base62：{@code KEY + 4VSLrykWrrM} 解密还原 {@code 332030}（decryptStr62）</li>
 * </ul>
 *
 * @since 2026/1/4
 * @version 1.0
 */
public class CDesUtilsTests {

    private static final String KEY = "Q9ucdr1x";

    private static final String PLAIN_TEXT = "332030";

    private static final String CIPHER_TEXT_64 = "NIAMvCzxs+A=";

    private static final String CIPHER_TEXT_62 = "4VSLrykWrrM";

    /**
     * 测试 DES 加密为 Base64 字符串
     * 对应测试用例 1.1：{@code KEY + "332030"} 加密得到 {@code NIAMvCzxs+A=}
     */
    @Test
    public void encryptStr64() {

        val cipherText = CDesUtils.encryptStr64(KEY, PLAIN_TEXT);
        Assertions.assertEquals(CIPHER_TEXT_64, cipherText);

    }

    /**
     * 测试 DES 加密为 Base62 字符串
     * 对应测试用例 1.2：{@code KEY + "332030"} 加密得到 {@code 4VSLrykWrrM}
     */
    @Test
    public void encryptStr62() {

        val cipherText = CDesUtils.encryptStr62(KEY, PLAIN_TEXT);
        Assertions.assertEquals(CIPHER_TEXT_62, cipherText);

    }

    /**
     * 测试 DES 解密 Base64 字符串
     * 对应测试用例 2.1：{@code KEY + NIAMvCzxs+A=} 解密还原 {@code 332030}
     */
    @Test
    public void decryptStr64() {

        val plainText = CDesUtils.decryptStr64(KEY, CIPHER_TEXT_64);
        Assertions.assertEquals(PLAIN_TEXT, plainText);

    }

    /**
     * 测试 DES 解密 Base62 字符串
     * 对应测试用例 2.2：{@code KEY + 4VSLrykWrrM} 解密还原 {@code 332030}
     */
    @Test
    public void decryptStr62() {

        val plainText = CDesUtils.decryptStr62(KEY, CIPHER_TEXT_62);
        Assertions.assertEquals(PLAIN_TEXT, plainText);

    }

}
