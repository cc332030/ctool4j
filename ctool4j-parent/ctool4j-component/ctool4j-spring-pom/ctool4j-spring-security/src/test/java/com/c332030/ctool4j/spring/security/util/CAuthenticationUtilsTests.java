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
 * @since 2026/8/14
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

    @Test
    void testEncode_returnsNonRawAndMatches() {
        String encoded = CAuthenticationUtils.encode("password123");
        Assertions.assertNotEquals("password123", encoded);
        Assertions.assertTrue(CAuthenticationUtils.matches("password123", encoded));
    }

    @Test
    void testMatches_correctRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertTrue(CAuthenticationUtils.matches("secret", encoded));
    }

    @Test
    void testMatches_wrongRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertFalse(CAuthenticationUtils.matches("wrong", encoded));
    }

    @Test
    void testMatches_nullRawPassword_throws() {
        // 易错：BCrypt 对 null rawPassword 抛 IllegalArgumentException
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CAuthenticationUtils.matches(null, encoded)
        );
    }

    @Test
    void testMatches_emptyRawPassword() {
        String encoded = CAuthenticationUtils.encode("secret");
        Assertions.assertFalse(CAuthenticationUtils.matches("", encoded));
    }

    @Test
    void testMatches_nullEncodedPassword_returnsFalse() {
        // 易错：BCrypt 对 null encodedPassword 直接返回 false，不抛异常
        Assertions.assertFalse(CAuthenticationUtils.matches("secret", null));
    }
}
