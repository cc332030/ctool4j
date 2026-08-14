package com.c332030.ctool4j.spring.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

/**
 * <p>
 * Description: CSpringSecurityUtilsTests
 * </p>
 * <p>
 * 仅测试基于 SecurityContextHolder 的纯逻辑方法，不依赖 Spring 容器。
 * </p>
 *
 * @since 2026/8/14
 */
class CSpringSecurityUtilsTests {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAuthentication_emptyContext_returnsNull() {
        SecurityContextHolder.clearContext();
        Assertions.assertNull(CSpringSecurityUtils.getAuthentication());
    }

    @Test
    void testGetPrincipal_emptyContext_returnsNull() {
        SecurityContextHolder.clearContext();
        Assertions.assertNull(CSpringSecurityUtils.getPrincipal());
    }

    @Test
    void testSetAndGetAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals(auth, CSpringSecurityUtils.getAuthentication());
    }

    @Test
    void testGetPrincipal_stringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals("admin", CSpringSecurityUtils.getPrincipal());
    }

    @Test
    void testGetPrincipal_userDetailsPrincipal() {
        UserDetails userDetails = new User("admin", "pass", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals(userDetails, CSpringSecurityUtils.getPrincipal());
    }

    @Test
    void testGetPrincipal_nullPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertNull(CSpringSecurityUtils.getPrincipal());
    }

    @Test
    void testGetUserDetails_throwsClassCast() {
        // 易错：getUserDetails 内部 anyType(getPrincipal()) 因泛型重载歧义匹配到
        // anyType(CSupplier) 重载，把 User 实例强转为 CSupplier，运行期抛 ClassCastException
        UserDetails userDetails = new User("admin", "pass", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertThrowsExactly(
            ClassCastException.class,
            CSpringSecurityUtils::getUserDetails
        );
    }

    @Test
    void testGetUserDetails_emptyContext_throwsNPE() {
        // 易错：空上下文时 getPrincipal 返回 null，anyType((CSupplier)null) 调用 supplier.get() 抛 NPE
        SecurityContextHolder.clearContext();
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            CSpringSecurityUtils::getUserDetails
        );
    }
}
