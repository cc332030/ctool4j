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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「编码 / 解码 / 往返一致性」三个维度组织：编码与解码分别验证正例与空值边界，往返一致性验证编码与解码互为逆操作、整体链路正确。</li>
 *   <li>空入参（null/空数组/空字符串）统一作为边界覆盖，验证与设计约定一致（返回 null 而非抛异常）。</li>
 *   <li>编码正例直接断言确定性的 Base64 期望值（{@code aGVsbG8=}），避免只测往返，确保编码结果稳定。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对空入参返回 null 的约定（encode 空字节数组/null、decode 空字符串/null）。</li>
 *   <li>依据方法互为逆操作的语义（roundTrip）。</li>
 *   <li>依据测试方法（等价类/边界值）：正例（非空字节数组/确定编码结果）+ 边界（null/空数组/空字符串）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：编码正例（确定性期望值）、编码空数组/null 返回 null、解码正例、解码空字符串/null 返回 null、往返一致性。</li>
 *   <li>未覆盖：非法 Base64 字符串的解码行为（底层 hutool 决定，本类不额外校验，见设计文档边界场景）。</li>
 * </ul>
 * <h2>编码（encode）</h2>
 * <ul>
 *   <li>1.1 正例：{@code hello} 字节数组编码得到确定性结果 {@code aGVsbG8=}（encode）</li>
 *   <li>1.2 边界：null 字节数组返回 null（encode）</li>
 *   <li>1.3 边界：空字节数组返回 null（encode）</li>
 * </ul>
 * <h2>解码（decode）</h2>
 * <ul>
 *   <li>2.1 正例：{@code aGVsbG8=} 解码还原 {@code hello} 字节数组（decode）</li>
 *   <li>2.2 边界：null 返回 null（decode）</li>
 *   <li>2.3 边界：空字符串返回 null（decode）</li>
 * </ul>
 * <h2>往返一致性（roundTrip）</h2>
 * <ul>
 *   <li>3.1 往返：encode 后 decode 恢复原字节数组，验证编解码互为逆操作（roundTrip）</li>
 * </ul>
 *
 * @since 2026/1/4
 * @version 1.0
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
     * 对应测试用例 3.1：往返：encode 后 decode 恢复原字节数组，验证编解码互为逆操作
     */
    @Test
    public void roundTrip() {

        byte[] origin = "ctool4j-base64".getBytes(StandardCharsets.UTF_8);
        String encoded = CBase64Utils.encode(origin);
        Assertions.assertArrayEquals(origin, CBase64Utils.decode(encoded));

    }

}
