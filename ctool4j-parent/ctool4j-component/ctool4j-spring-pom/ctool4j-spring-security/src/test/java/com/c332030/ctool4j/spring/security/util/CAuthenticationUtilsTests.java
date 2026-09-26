package com.c332030.ctool4j.spring.security.util;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.SpringVersion;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>
 * Description: CAuthenticationUtilsTests
 * </p>
 * <p>
 * 通过注入真实 BCryptPasswordEncoder 测试 encode/matches 纯逻辑，不依赖 Spring 容器。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证密码编码与校验的各条路径与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对密码编码与校验的约定。</li>
 *   <li>依据测试方法（等价类/边界/分支覆盖）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：密码编码与校验的正常、边界与异常路径。</li>
 *   <li>未覆盖：真实容器/框架集成场景。</li>
 * </ul>
 * <h2>认证工具</h2>
 * <ul>
 *   <li>1.1 验证密码编码与校验（对应测试方法 1.1-1.6）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CAuthenticationUtilsTests {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        CAuthenticationUtils.setPasswordEncoder(encoder);
    }

    @AfterEach
    void tearDown() {
        CAuthenticationUtils.setPasswordEncoder(null);
    }

        /**
         * 对应测试用例 1.1：验证密码编码与校验（对应测试方法 1.1-1.6）
         */
    @Test
    void testEncode_returnsNonRawAndMatches() {
        String encoded = CAuthenticationUtils.encode("password123");
        Assertions.assertNotEquals("password123", encoded);
        Assertions.assertTrue(CAuthenticationUtils.matches("password123", encoded));
    }

        /**
         * 对应测试用例 1.2
         */
    @Test
    void testMatches_correctRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertTrue(CAuthenticationUtils.matches("secret", encoded));
    }

        /**
         * 对应测试用例 1.3
         */
    @Test
    void testMatches_wrongRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertFalse(CAuthenticationUtils.matches("wrong", encoded));
    }

        /**
         * 对应测试用例 1.4
         */
    @Test
    void testMatches_nullRawPassword_versionDependent() {
        // 易错：null rawPassword 的处置随 Spring Security 版本而异（已登记 doc/compatibility.adoc）：
        //   - Spring Security 6（Boot 2.7 基线）：BCrypt 对 null rawPassword 抛 IllegalArgumentException
        //   - Spring Security 7（当前最新 LTS 档位）：改用 AbstractValidatingPasswordEncoder，
        //     null 参数（rawPassword 与 encodedPassword）统一返回 false，不再抛异常
        // 本用例按运行时 Spring Security 版本判定期望值，两个档位下都能通过。
        // 使用方不应依赖"抛异常"来判空，需自行在入参侧校验。
        String encoded = CAuthenticationUtils.encode("secret");
        if (nullParameterThrows()) {
            Assertions.assertThrowsExactly(
                IllegalArgumentException.class,
                () -> CAuthenticationUtils.matches(null, encoded)
            );
        } else {
            Assertions.assertFalse(CAuthenticationUtils.matches(null, encoded));
        }
    }

    /**
     * 当前 Spring Security 版本是否对 null 入参抛 {@code IllegalArgumentException}
     *
     * <p>Spring Security 7 起 {@code BCryptPasswordEncoder} 改继承
     * {@code AbstractValidatingPasswordEncoder}，null 参数统一返回 {@code false}；
     * 6.x 及以前抛 {@code IllegalArgumentException}。此处按版本主号判定。</p>
     *
     * @return true 表示 null 入参抛 IllegalArgumentException（6.x 及以前）
     */
    private static boolean nullParameterThrows() {
        val major = Integer.parseInt(SpringVersion.getVersion().split("\\.")[0]);
        return major < 7;
    }

        /**
         * 对应测试用例 1.5
         */
    @Test
    void testMatches_emptyRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertFalse(CAuthenticationUtils.matches("", encoded));
    }

        /**
         * 对应测试用例 1.6
         */
    @Test
    void testMatches_nullEncodedPassword_returnsFalse() {
        // 易错：BCrypt 对 null encodedPassword 直接返回 false，不抛异常
        Assertions.assertFalse(CAuthenticationUtils.matches("secret", null));
    }
}
