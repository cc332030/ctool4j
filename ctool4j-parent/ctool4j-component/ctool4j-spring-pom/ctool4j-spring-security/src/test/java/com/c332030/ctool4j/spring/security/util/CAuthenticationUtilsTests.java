package com.c332030.ctool4j.spring.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void testMatches_nullRawPassword_throws() {
        // 易错：BCrypt 对 null rawPassword 抛 IllegalArgumentException
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CAuthenticationUtils.matches(null, encoded)
        );
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
