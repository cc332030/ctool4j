package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.util.CIpUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * Description: CIpUtilsTests
 * </p>
 * <p>
 * 纯逻辑单元测试，不依赖 Spring 容器/Web 上下文。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按规则形式（单个 IP / CIDR 网段 / 混合）与输入合法性（合法/非法/空）两组维度交叉覆盖。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code contains} 的约定：命中任一规则返回 true；IP 或规则非法采用"拒绝/跳过"策略。</li>
 *   <li>依据等价类/边界值：CIDR 前缀边界（/0、/24、/25、/32）、网段首末地址、越界地址、非法 IP 段、非法前缀。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：单 IP 命中/未命中；CIDR 网段内/边界/网段外；前缀 /0、/30、/31、/32、/25、/8；多规则混合命中；非法/空 IP；非法规则跳过；空规则集合；</li>
 *   <li>IPv6 地址与 IPv6 规则优雅拒绝（返回 false、不抛异常）；极端 IPv4 边界（0.0.0.0、255.255.255.255）；非标准 CIDR 起点。</li>
 *   <li>未覆盖：真实容器/拦截器集成场景（由拦截器测试/集成用例另行覆盖）、IPv6 的实际支持（当前仅验证"优雅拒绝"）。</li>
 * </ul>
 * <h2>IP 判断</h2>
 * <ul>
 *   <li>1.1 单个 IP：命中、未命中</li>
 *   <li>1.2 CIDR 网段：网段内、边界、网段外</li>
 *   <li>1.3 CIDR 前缀边界：/0（全量）、/32（单 IP）、/25（非 8 整数倍）</li>
 *   <li>1.4 多规则混合：命中任一</li>
 *   <li>1.5 集合重载：Collection 入参</li>
 *   <li>1.6 非法/空输入：IP 空/越界/非数字返回 false</li>
 *   <li>1.7 非法规则：被跳过，不影响其它合法规则；全部非法返回 false</li>
 *   <li>1.8 空规则：返回 false；规则含空白项时跳过</li>
 *   <li>1.9 IPv6 优雅拒绝：IPv6 地址/规则返回 false 且不抛异常；IPv6 规则被跳过不影响其它 IPv4 规则</li>
 *   <li>1.10 极端 IPv4 边界：0.0.0.0、255.255.255.255（单 IP 与 /32，验证哨兵不误判）</li>
 *   <li>1.11 更多掩码位：/30（4 地址）、/31（2 地址）、/8</li>
 *   <li>1.12 非标准 CIDR 起点：如 192.168.1.5/24，按输入 IP+掩码由 hutool 计算区间</li>
 *   <li>1.13 单 IP 段边界：段值为 0 与 255</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/9
 * @version 1.0
 */
class CIpUtilsTests {

    // ---------- 单个 IP ----------

    /**
     * 对应测试用例 1.1：命中单个 IP
     */
    @Test
    void contains_singleIp_match() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "192.168.1.5"));
    }

    /**
     * 对应测试用例 1.1：单个 IP 未命中
     */
    @Test
    void contains_singleIp_notMatch() {
        Assertions.assertFalse(CIpUtils.contains("192.168.1.6", "192.168.1.5"));
    }

    // ---------- CIDR 网段 ----------

    /**
     * 对应测试用例 1.2：命中 CIDR 网段内
     */
    @Test
    void contains_cidr_inRange() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "192.168.1.0/24"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.255", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.2：CIDR 网段边界
     */
    @Test
    void contains_cidr_boundary() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.0", "192.168.1.0/24"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.255", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.2：CIDR 网段外
     */
    @Test
    void contains_cidr_outOfRange() {
        Assertions.assertFalse(CIpUtils.contains("192.168.2.1", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.3：CIDR 前缀 /0：全量
     */
    @Test
    void contains_cidr_prefix0() {
        Assertions.assertTrue(CIpUtils.contains("8.8.8.8", "0.0.0.0/0"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.1", "0.0.0.0/0"));
    }

    /**
     * 对应测试用例 1.3：CIDR 前缀 /32：等价单个 IP
     */
    @Test
    void contains_cidr_prefix32() {
        Assertions.assertTrue(CIpUtils.contains("10.0.0.1", "10.0.0.1/32"));
        Assertions.assertFalse(CIpUtils.contains("10.0.0.2", "10.0.0.1/32"));
    }

    /**
     * 对应测试用例 1.3：CIDR 非 8 整数倍前缀
     */
    @Test
    void contains_cidr_nonBytePrefix() {
        // 192.168.1.128/25 => 192.168.1.128 ~ 192.168.1.255
        Assertions.assertTrue(CIpUtils.contains("192.168.1.129", "192.168.1.128/25"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.255", "192.168.1.128/25"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.127", "192.168.1.128/25"));
    }

    // ---------- 多规则混合 ----------

    /**
     * 对应测试用例 1.4：命中多规则中任一
     */
    @Test
    void contains_multipleRules_any() {
        Assertions.assertTrue(
            CIpUtils.contains("192.168.1.5", "10.0.0.0/8", "192.168.1.0/24")
        );
        Assertions.assertTrue(CIpUtils.contains("10.2.3.4", "10.0.0.0/8", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("172.16.0.1", "10.0.0.0/8", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.5：集合重载
     */
    @Test
    void contains_collection() {
        Assertions.assertTrue(
            CIpUtils.contains("127.0.0.1", Arrays.asList("127.0.0.1", "192.168.1.0/24"))
        );
        Assertions.assertFalse(CIpUtils.contains("8.8.8.8", Collections.emptyList()));
    }

    // ---------- 非法/空输入 ----------

    /**
     * 对应测试用例 1.6：非法 IP（段越界/非数字/空串）返回 false
     */
    @Test
    void contains_invalidIp_false() {
        Assertions.assertFalse(CIpUtils.contains("", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("  ", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains(null, "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.256", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("abc.def.ghi.jkl", "192.168.1.0/24"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.a", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.6：空/非法 IP 即使命中单 IP 规则也返回 false
     */
    @Test
    void contains_invalidIpWithMatchRule_false() {
        Assertions.assertFalse(CIpUtils.contains("", "192.168.1.5"));
    }

    /**
     * 对应测试用例 1.7：非法规则被跳过，不影响其它合法规则
     */
    @Test
    void contains_invalidRule_skipped() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "not-a-rule", "192.168.1.0/24"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "192.168.1.999", "192.168.1.5"));
    }

    /**
     * 对应测试用例 1.7：全部规则非法时返回 false
     */
    @Test
    void contains_allInvalidRules_false() {
        Assertions.assertFalse(CIpUtils.contains("192.168.1.5", "192.168.1.999/50", "abc"));
    }

    /**
     * 对应测试用例 1.8：rules 为空时返回 false
     */
    @Test
    void contains_emptyRules_false() {
        Assertions.assertFalse(CIpUtils.contains("192.168.1.5"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.5"));
    }

    /**
     * 对应测试用例 1.8：rules 含空白项时跳过
     */
    @Test
    void contains_blankRule_skipped() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", " ", "192.168.1.5"));
    }

    // ---------- IPv6（当前仅支持 IPv4，需优雅拒绝，不抛异常） ----------

    /**
     * 对应测试用例 1.9：IPv6 地址传入返回 false，不抛异常
     */
    @Test
    void contains_ipv6_false_noException() {
        Assertions.assertDoesNotThrow(() ->
            Assertions.assertFalse(CIpUtils.contains("::1", "192.168.1.0/24"))
        );
        Assertions.assertFalse(CIpUtils.contains("fe80::1", "192.168.1.5"));
        Assertions.assertFalse(CIpUtils.contains("2001:db8::1", "0.0.0.0/0"));
    }

    /**
     * 对应测试用例 1.9：IPv6 规则不产生异常，且 v4 地址不命中 IPv6 规则
     */
    @Test
    void contains_ipv6Rule_noException_notMatch() {
        Assertions.assertDoesNotThrow(() ->
            Assertions.assertFalse(CIpUtils.contains("192.168.1.5", "::1/128"))
        );
        Assertions.assertDoesNotThrow(() ->
            Assertions.assertFalse(CIpUtils.contains("192.168.1.5", "2001:db8::/32"))
        );
    }

    /**
     * 对应测试用例 1.9：IPv6 规则被跳过，不影响其它合法 IPv4 规则
     */
    @Test
    void contains_ipv6Rule_skipped_v4StillWorks() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "::1/128", "192.168.1.0/24"));
    }

    /**
     * 对应测试用例 1.9：纯 IPv6 规则的集合，IPv4 地址不命中
     */
    @Test
    void contains_allIpv6Rules_false() {
        Assertions.assertDoesNotThrow(() ->
            Assertions.assertFalse(CIpUtils.contains("192.168.1.5", "fe80::/10", "::1/128"))
        );
    }

    // ---------- 极端 IPv4 边界 ----------

    /**
     * 对应测试用例 1.10：IPv4 全 0 地址
     */
    @Test
    void contains_ipv4_zeroAddress() {
        Assertions.assertTrue(CIpUtils.contains("0.0.0.0", "0.0.0.0"));
        Assertions.assertTrue(CIpUtils.contains("0.0.0.0", "0.0.0.0/0"));
    }

    /**
     * 对应测试用例 1.10：IPv4 全 255 地址（0xFFFFFFFF，验证哨兵 INVALID_IP=-1 不误判）
     */
    @Test
    void contains_ipv4_maxAddress() {
        Assertions.assertTrue(CIpUtils.contains("255.255.255.255", "255.255.255.255"));
        Assertions.assertTrue(CIpUtils.contains("255.255.255.255", "255.255.255.255/32"));
    }

    // ---------- 更多掩码位 ----------

    /**
     * 对应测试用例 1.11：/30 网段（4 个地址）
     */
    @Test
    void contains_cidr_prefix30() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.0", "192.168.1.0/30"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.3", "192.168.1.0/30"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.4", "192.168.1.0/30"));
    }

    /**
     * 对应测试用例 1.11：/31 网段（2 个地址）
     */
    @Test
    void contains_cidr_prefix31() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.0", "192.168.1.0/31"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.1", "192.168.1.0/31"));
        Assertions.assertFalse(CIpUtils.contains("192.168.1.2", "192.168.1.0/31"));
    }

    /**
     * 对应测试用例 1.11：/8 网段
     */
    @Test
    void contains_cidr_prefix8() {
        Assertions.assertTrue(CIpUtils.contains("10.0.0.1", "10.0.0.0/8"));
        Assertions.assertTrue(CIpUtils.contains("10.255.255.255", "10.0.0.0/8"));
        Assertions.assertFalse(CIpUtils.contains("11.0.0.1", "10.0.0.0/8"));
    }

    // ---------- 非标准 CIDR 起点 ----------

    /**
     * 对应测试用例 1.12：CIDR 网络地址非规范起点（如 192.168.1.5/24），按输入 IP 与掩码由 hutool 计算区间，仍命中该 IP
     */
    @Test
    void contains_cidr_nonCanonicalBase() {
        Assertions.assertTrue(CIpUtils.contains("192.168.1.5", "192.168.1.5/24"));
        Assertions.assertTrue(CIpUtils.contains("192.168.1.200", "192.168.1.5/24"));
        Assertions.assertFalse(CIpUtils.contains("192.168.2.1", "192.168.1.5/24"));
    }

    /**
     * 对应测试用例 1.13：单 IP 段边界 0 与 255
     */
    @Test
    void contains_singleIp_segmentBoundary() {
        Assertions.assertTrue(CIpUtils.contains("1.0.1.1", "1.0.1.1"));
        Assertions.assertFalse(CIpUtils.contains("1.0.0.1", "1.0.1.1"));
    }

}
